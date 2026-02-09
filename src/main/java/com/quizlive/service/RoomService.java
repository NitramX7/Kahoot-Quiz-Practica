package com.quizlive.service;

import com.quizlive.model.*;
import com.quizlive.repository.QuestionRepository;
import com.quizlive.repository.RoomQuestionRepository;
import com.quizlive.repository.RoomRepository;
import com.quizlive.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomQuestionRepository roomQuestionRepository;
    private final QuestionRepository questionRepository;
    private final BlockService blockService;
    private final PlayerRepository playerRepository;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public Room createRoom(Long blockId, Integer numQuestions, Room.SelectionMode selectionMode,
                          Integer timePerQuestion, User host, List<Long> manualQuestionIds) {
        Block block = blockService.getBlockById(blockId, host.getId());

        // Validar que el bloque tiene suficientes preguntas
        // Las preguntas pueden ser reutilizadas en múltiples salas
        if (numQuestions > block.getQuestions().size()) {
            throw new IllegalArgumentException("Number of questions exceeds available questions in block");
        }

        String pin = generateUniquePin();

        Room room = new Room();
        room.setPin(pin);
        room.setHost(host);
        room.setBlock(block);
        room.setNumQuestions(numQuestions);
        room.setSelectionMode(selectionMode);
        room.setTimePerQuestion(timePerQuestion);
        room.setState(Room.RoomState.WAITING);

        Room savedRoom = roomRepository.save(room);

        selectQuestionsForRoom(savedRoom, selectionMode, numQuestions, manualQuestionIds);

        log.info("Created room with PIN {} for user {}", pin, host.getUsername());
        return savedRoom;
    }

    @Transactional
    public void selectQuestionsForRoom(Room room, Room.SelectionMode mode, 
                                        int numQuestions, List<Long> manualQuestionIds) {
        List<Question> selectedQuestions;

        if (mode == Room.SelectionMode.MANUAL) {
            if (manualQuestionIds == null || manualQuestionIds.size() != numQuestions) {
                throw new IllegalArgumentException("Must provide exactly " + numQuestions + " question IDs for manual mode");
            }
            selectedQuestions = questionRepository.findAllById(manualQuestionIds);
        } else {
            List<Question> allQuestions = room.getBlock().getQuestions();
            selectedQuestions = selectRandomQuestions(allQuestions, numQuestions);
        }

        int orderNum = 1;
        for (Question question : selectedQuestions) {
            RoomQuestion roomQuestion = new RoomQuestion();
            roomQuestion.setRoom(room);
            roomQuestion.setQuestion(question);
            roomQuestion.setOrderNum(orderNum++);
            roomQuestion.setIsOpen(false);
            roomQuestionRepository.save(roomQuestion);
        }
    }

    private String generateUniquePin() {
        String pin;
        do {
            pin = String.format("%04d", random.nextInt(10000));
        } while (roomRepository.existsByPin(pin));
        return pin;
    }

    private List<Question> selectRandomQuestions(List<Question> questions, int count) {
        List<Question> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled);
        return shuffled.stream().limit(count).collect(Collectors.toList());
    }

    public Room getRoomByPin(String pin) {
        return roomRepository.findByPin(pin)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with PIN: " + pin));
    }

    public Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
    }

    public List<Room> getRoomsByHost(Long hostId) {
        return roomRepository.findByHostId(hostId);
    }

    public List<RoomQuestion> getRoomQuestions(Long roomId) {
        return roomQuestionRepository.findByRoomIdOrderByOrderNumAsc(roomId);
    }

    @Transactional
    public void startRoom(Long roomId, Long hostId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        
        if (!room.getHost().getId().equals(hostId)) {
            throw new SecurityException("Only the host can start the room");
        }

        room.start();
        roomRepository.save(room);
        log.info("[Room {}] Game started by host {}", room.getPin(), room.getHost().getUsername());
    }

    @Transactional
    public void finishRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        
        room.finish();
        roomRepository.save(room);
        log.info("[Room {}] Game finished", room.getPin());
    }

    @Transactional
    public RoomQuestion getNextQuestion(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (!room.isRunning()) {
            throw new IllegalStateException("Room is not running");
        }

        List<RoomQuestion> questions = roomQuestionRepository.findByRoomIdOrderByOrderNumAsc(roomId);
        
        Optional<RoomQuestion> currentOpen = questions.stream()
                .filter(RoomQuestion::getIsOpen)
                .findFirst();
        
        currentOpen.ifPresent(RoomQuestion::close);

        Optional<RoomQuestion> nextQuestion = questions.stream()
                .filter(rq -> rq.getStartTime() == null)
                .findFirst();

        if (nextQuestion.isPresent()) {
            RoomQuestion q = nextQuestion.get();
            q.open();
            roomQuestionRepository.save(q);
            log.info("[Room {}] Opened question {}", room.getPin(), q.getOrderNum());
            return q;
        } else {
            log.info("[Room {}] ¡Todas las preguntas respondidas! Mostrando podio al usuario...", room.getPin());
            finishRoom(roomId);
            return null;
        }
    }

    public RoomQuestion getCurrentQuestion(Long roomId) {
        return roomQuestionRepository.findByRoomIdOrderByOrderNumAsc(roomId).stream()
                .filter(rq -> rq.getIsOpen() || (rq.getStartTime() != null && rq.getCloseTime() != null))
                .reduce((first, second) -> second)
                .orElse(null);
    }
    
    public RoomQuestion getActiveQuestion(Long roomId) {
         return roomQuestionRepository.findByRoomIdOrderByOrderNumAsc(roomId).stream()
                .filter(RoomQuestion::getIsOpen)
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void submitAnswer(Long roomId, Long playerId, Long roomQuestionId, Integer selectedOption) {
        RoomQuestion roomQuestion = roomQuestionRepository.findById(roomQuestionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        
        if (!roomQuestion.canAcceptAnswers()) {
            throw new IllegalStateException("Question is not open for answers");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        if (player.hasAnsweredQuestion(roomQuestionId)) {
           throw new IllegalStateException("Player already answered this question");
        }

        Answer answer = new Answer();
        answer.setPlayer(player);
        answer.setRoomQuestion(roomQuestion);
        answer.setSelectedOption(selectedOption);
        
        long diff = java.time.Duration.between(roomQuestion.getStartTime(), java.time.LocalDateTime.now()).toMillis();
        answer.setResponseTime(diff);

        Question originalQuestion = roomQuestion.getQuestion();
        boolean isCorrect = originalQuestion.getCorrectOption().equals(selectedOption);
        answer.setIsCorrect(isCorrect);
        
        answer.calculatePoints(true, roomQuestion.getRoom().getTimePerQuestion());
        
        player.addScore(answer.getPointsEarned());
        player.getAnswers().add(answer);
        
        playerRepository.save(player);
    }
}