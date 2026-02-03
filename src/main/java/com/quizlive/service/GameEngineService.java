package com.quizlive.service;

import com.quizlive.model.*;
import com.quizlive.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameEngineService {

    private final RoomRepository roomRepository;
    private final RoomQuestionRepository roomQuestionRepository;
    private final PlayerRepository playerRepository;
    private final AnswerRepository answerRepository;
    private final RoomService roomService;

    @Qualifier("timerExecutor")
    private final ScheduledExecutorService timerExecutor;

    @Qualifier("answerProcessingExecutor")
    private final ExecutorService answerProcessingExecutor;

    private final ConcurrentHashMap<String, RoomState> activeRooms = new ConcurrentHashMap<>();

    private class RoomState {
        private final String pin;
        private final Long roomId;
        private final List<RoomQuestion> questions;
        private final AtomicInteger currentQuestionIndex;
        private final int timePerQuestion;
        
        private final ConcurrentHashMap<Long, Integer> playerScores;
        private final ConcurrentHashMap<Long, Set<Long>> playerAnsweredQuestions;
        
        private ScheduledFuture<?> currentTimer;
        private final Object questionLock = new Object();
        
        public RoomState(String pin, Long roomId, List<RoomQuestion> questions, int timePerQuestion) {
            this.pin = pin;
            this.roomId = roomId;
            this.questions = questions;
            this.currentQuestionIndex = new AtomicInteger(0);
            this.timePerQuestion = timePerQuestion;
            this.playerScores = new ConcurrentHashMap<>();
            this.playerAnsweredQuestions = new ConcurrentHashMap<>();
        }

        public RoomQuestion getCurrentQuestion() {
            int index = currentQuestionIndex.get();
            if (index < questions.size()) {
                return questions.get(index);
            }
            return null;
        }

        public boolean hasMoreQuestions() {
            return currentQuestionIndex.get() < questions.size();
        }

        public void moveToNextQuestion() {
            currentQuestionIndex.incrementAndGet();
        }

        // Método ATÓMICO usando operaciones atómicas de ConcurrentHashMap
        // Retorna true si se registró exitosamente, false si ya había respondido
        public boolean tryRecordPlayerAnswer(Long playerId, Long roomQuestionId, int points) {
            log.debug("[tryRecordPlayerAnswer] Player {} attempting to answer question {}", playerId, roomQuestionId);
            
            Set<Long> answeredQuestions = playerAnsweredQuestions.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet());
            
            log.debug("[tryRecordPlayerAnswer] Player {} currently has answered: {}", playerId, answeredQuestions);
            
            // add() en ConcurrentHashMap.newKeySet() es atómico
            // Retorna true si se agregó, false si ya existía
            boolean wasAdded = answeredQuestions.add(roomQuestionId);
            
            log.debug("[tryRecordPlayerAnswer] Player {} add result: {} (question {})", playerId, wasAdded, roomQuestionId);
            
            if (wasAdded) {
                // Solo actualizar puntos si realmente agregamos la respuesta
                playerScores.merge(playerId, points, Integer::sum);
                log.info("[tryRecordPlayerAnswer] Player {} successfully recorded answer to question {}, earned {} points", 
                        playerId, roomQuestionId, points);
            } else {
                log.warn("[tryRecordPlayerAnswer] Player {} already answered question {}", playerId, roomQuestionId);
            }
            
            return wasAdded;
        }

        public void cancelTimer() {
            if (currentTimer != null && !currentTimer.isDone()) {
                currentTimer.cancel(false);
            }
        }
    }

    @Transactional
    public void startGame(String pin) {
        setMDC(pin);
        log.info("[Thread: {}] Starting game", Thread.currentThread().getName());

        Room room = roomService.getRoomByPin(pin);
        if (!room.isWaiting()) {
            throw new IllegalStateException("Room is not in WAITING state");
        }

        List<RoomQuestion> questions = roomQuestionRepository.findByRoomIdOrderByOrderNumAsc(room.getId());
        if (questions.isEmpty()) {
            throw new IllegalStateException("No questions configured for this room");
        }

        RoomState roomState = new RoomState(pin, room.getId(), questions, room.getTimePerQuestion());
        activeRooms.put(pin, roomState);

        room.start();
        roomRepository.save(room);

        log.info("[Thread: {}] Game initialized with {} questions", 
                Thread.currentThread().getName(), questions.size());

        startNextQuestion(pin);
        clearMDC();
    }

    @Transactional
    public void startNextQuestion(String pin) {
        setMDC(pin);
        RoomState roomState = activeRooms.get(pin);
        if (roomState == null) {
            log.warn("[Thread: {}] Room state not found", Thread.currentThread().getName());
            clearMDC();
            return;
        }

        if (!roomState.hasMoreQuestions()) {
            log.info("[Thread: {}] All questions completed, finishing game", Thread.currentThread().getName());
            finishGame(pin);
            clearMDC();
            return;
        }

        synchronized (roomState.questionLock) {
            RoomQuestion question = roomState.getCurrentQuestion();
            if (question == null) {
                clearMDC();
                return;
            }

            question.open();
            roomQuestionRepository.save(question);

            log.info("[Thread: {}] Question {} opened (order: {})", 
                    Thread.currentThread().getName(), question.getId(), question.getOrderNum());

            roomState.currentTimer = timerExecutor.schedule(() -> {
                setMDC(pin);
                log.info("[Thread: {}] Timer expired for question {}", 
                        Thread.currentThread().getName(), question.getId());
                closeQuestion(pin, question.getId());
                clearMDC();
            }, roomState.timePerQuestion, TimeUnit.SECONDS);

            log.info("[Thread: {}] Timer scheduled for {} seconds", 
                    Thread.currentThread().getName(), roomState.timePerQuestion);
        }
        clearMDC();
    }

    @Transactional
    public void closeQuestion(String pin, Long roomQuestionId) {
        setMDC(pin);
        RoomState roomState = activeRooms.get(pin);
        if (roomState == null) {
            clearMDC();
            return;
        }

        synchronized (roomState.questionLock) {
            RoomQuestion question = roomQuestionRepository.findById(roomQuestionId).orElse(null);
            if (question != null && question.getIsOpen()) {
                question.close();
                roomQuestionRepository.save(question);
                log.info("[Thread: {}] Question {} closed", Thread.currentThread().getName(), roomQuestionId);
            }

            roomState.cancelTimer();

            roomState.moveToNextQuestion();
            
            timerExecutor.schedule(() -> startNextQuestion(pin), 2, TimeUnit.SECONDS);
        }
        clearMDC();
    }

    public CompletableFuture<Answer> submitAnswer(String pin, String playerName, 
                                                    Long roomQuestionId, Integer selectedOption) {
        return CompletableFuture.supplyAsync(() -> {
            return processAnswer(pin, playerName, roomQuestionId, selectedOption);
        }, answerProcessingExecutor)
                .thenApply(answer -> {
                    try {
                        RoomState roomState = activeRooms.get(pin);
                        if (roomState != null) {
                            long totalPlayers = playerRepository.countByRoomId(roomState.roomId);
                            int answersCount = roomState.playerAnsweredQuestions.entrySet().stream()
                                    .filter(entry -> entry.getValue().contains(roomQuestionId))
                                    .mapToInt(e -> 1)
                                    .sum();
                            
                            log.debug("[Thread: {}] Progress check: {}/{} answers for question {}", 
                                    Thread.currentThread().getName(), answersCount, totalPlayers, roomQuestionId);

                            if (answersCount >= totalPlayers) {
                                log.info("[Thread: {}] All active players ({}) answered. Closing question early.", 
                                        Thread.currentThread().getName(), totalPlayers);
                                timerExecutor.schedule(() -> closeQuestion(pin, roomQuestionId), 1, TimeUnit.SECONDS);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error checking auto-advance condition", e);
                    }
                    return answer;
                });
    }
    
    @Transactional
    private Answer processAnswer(String pin, String playerName, Long roomQuestionId, Integer selectedOption) {
        setMDC(pin);
        long startTime = System.currentTimeMillis();
        
        log.info("[Thread: {}] Processing answer from player '{}' for question {}", 
                Thread.currentThread().getName(), playerName, roomQuestionId);

        try {
            RoomState roomState = activeRooms.get(pin);
            if (roomState == null) {
                throw new IllegalStateException("Room not active");
            }

            RoomQuestion roomQuestion = roomQuestionRepository.findByIdWithQuestion(roomQuestionId)
                    .orElseThrow(() -> new IllegalArgumentException("Question not found"));

            if (!roomQuestion.canAcceptAnswers()) {
                log.warn("[Thread: {}] Question {} is closed, rejecting answer", 
                        Thread.currentThread().getName(), roomQuestionId);
                throw new IllegalStateException("Question is no longer accepting answers");
            }

            Player player = playerRepository.findByRoomPinAndName(pin, playerName)
                    .orElseThrow(() -> new IllegalArgumentException("Player not found"));

            log.info("[processAnswer] Found player - ID: {}, Name: '{}', Pin: '{}'", 
                    player.getId(), player.getName(), pin);

            long responseTime = System.currentTimeMillis() - roomQuestion.getStartTime()
                    .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

            Question question = roomQuestion.getQuestion();
            boolean isCorrect = question.isCorrect(selectedOption);

            Answer answer = new Answer();
            answer.setPlayer(player);
            answer.setRoomQuestion(roomQuestion);
            answer.setSelectedOption(selectedOption);
            answer.setResponseTime(responseTime);
            answer.setIsCorrect(isCorrect);
            
            answer.calculatePoints(false, roomState.timePerQuestion);
            
            log.debug("[processAnswer] About to try recording - Player ID: {}, Question ID: {}, Points: {}", 
                    player.getId(), roomQuestionId, answer.getPointsEarned());
            
            // Intentar registrar de forma ATÓMICA (verifica + registra en una operación)
            // Si retorna false, significa que ya respondió (race condition evitada)
            if (!roomState.tryRecordPlayerAnswer(player.getId(), roomQuestionId, answer.getPointsEarned())) {
                log.warn("[Thread: {}] Player {} already answered question {} (detected in atomic operation)", 
                        Thread.currentThread().getName(), playerName, roomQuestionId);
                throw new IllegalStateException("You have already answered this question");
            }
            
            // Solo llegamos aquí si el registro fue exitoso
            // Ahora guardar en base de datos
            Answer savedAnswer = answerRepository.save(answer);

            player.addScore(answer.getPointsEarned());
            playerRepository.save(player);
            playerRepository.flush();

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("[Thread: {}] Answer processed in {}ms - Player: {}, Correct: {}, Points: {}", 
                    Thread.currentThread().getName(), processingTime, playerName, 
                    isCorrect, answer.getPointsEarned());

            return savedAnswer;

        } catch (Exception e) {
            log.error("[Thread: {}] Error processing answer: {}", 
                    Thread.currentThread().getName(), e.getMessage());
            throw e;
        } finally {
            clearMDC();
        }
    }

    @Transactional
    public void finishGame(String pin) {
        setMDC(pin);
        log.info("[Thread: {}] Finishing game", Thread.currentThread().getName());

        RoomState roomState = activeRooms.get(pin);
        if (roomState != null) {
            roomState.cancelTimer();
            activeRooms.remove(pin);
        }

        Room room = roomService.getRoomByPin(pin);
        room.finish();
        roomRepository.save(room);

        log.info("[Thread: {}] Game finished successfully", Thread.currentThread().getName());
        clearMDC();
    }

    public RoomQuestion getCurrentQuestion(String pin) {
        RoomState roomState = activeRooms.get(pin);
        if (roomState == null) {
            throw new IllegalStateException("Room not active");
        }
        RoomQuestion current = roomState.getCurrentQuestion();
        if (current == null) {
            return null;
        }
        return roomQuestionRepository.findByIdWithQuestion(current.getId()).orElse(current);
    }

    public List<Player> getRanking(String pin) {
        Room room = roomService.getRoomByPin(pin);
        return playerRepository.findByRoomIdOrderByScoreDesc(room.getId());
    }

    private void setMDC(String pin) {
        MDC.put("roomPin", pin);
    }

    private void clearMDC() {
        MDC.clear();
    }

    public boolean isRoomActive(String pin) {
        return activeRooms.containsKey(pin);
    }

    public Set<String> getActiveRoomPins() {
        return new HashSet<>(activeRooms.keySet());
    }
}