package com.quizlive.controller;

import com.quizlive.model.Player;
import com.quizlive.model.Room;
import com.quizlive.model.RoomQuestion;
import com.quizlive.repository.AnswerRepository;
import com.quizlive.service.GameEngineService;
import com.quizlive.service.PlayerService;
import com.quizlive.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomApiController {

    private final PlayerService playerService;
    private final RoomService roomService;
    private final GameEngineService gameEngineService;
    private final AnswerRepository answerRepository;

    @GetMapping("/{roomId}/players")
    public List<Player> getPlayers(@PathVariable Long roomId) {
        return playerService.getPlayersByRoom(roomId);
    }

    @GetMapping("/{pin}/status")
    public Map<String, Object> getRoomStatus(@PathVariable String pin) {
        Room room = roomService.getRoomByPin(pin);
        Map<String, Object> status = new HashMap<>();
        status.put("state", room.getState().toString());
        status.put("pin", room.getPin());
        return status;
    }
    @PostMapping("/{roomId}/next-question")
    public Map<String, Object> nextQuestion(@PathVariable Long roomId) {
        var question = roomService.getNextQuestion(roomId);
        Map<String, Object> response = new HashMap<>();
        if (question != null) {
            response.put("finished", false);
            response.put("questionId", question.getId());
        } else {
            response.put("finished", true);
        }
        return response;
    }

    @PostMapping("/{roomId}/submit-answer")
    public Map<String, Object> submitAnswer(@PathVariable Long roomId, @RequestBody Map<String, Object> payload) {
        Long playerId = Long.valueOf(payload.get("playerId").toString());
        Long questionId = Long.valueOf(payload.get("questionId").toString());
        Integer selectedOption = Integer.valueOf(payload.get("selectedOption").toString());

        Room room = roomService.getRoomById(roomId);
        Player player = playerService.getPlayerById(playerId);
        var answer = gameEngineService.submitAnswer(room.getPin(), player.getName(), questionId, selectedOption).join();
        Map<String, Object> response = new HashMap<>();
        response.put("correct", answer.getIsCorrect());
        response.put("points", answer.getPointsEarned());
        response.put("totalScore", answer.getPlayer() != null ? answer.getPlayer().getScore() : 0);
        return response;
    }

    @GetMapping("/{roomId}/current-question")
    public Map<String, Object> getCurrentQuestion(@PathVariable Long roomId) {
        Room room = roomService.getRoomById(roomId);
        Map<String, Object> response = new HashMap<>();

        if (room.isFinished()) {
            response.put("finished", true);
            response.put("state", "FINISHED");
            return response;
        }

        RoomQuestion question;
        try {
            question = gameEngineService.getCurrentQuestion(room.getPin());
        } catch (Exception e) {
            question = null;
        }
        
        if (question != null) {
            response.put("id", question.getId());
            response.put("text", question.getQuestion().getText());
            response.put("isOpen", question.getIsOpen());
            response.put("answersCount", answerRepository.countByRoomQuestionId(question.getId()));
            response.put("finished", false);
            // No enviar la opción correcta a los jugadores si no es seguro, pero por simplicidad se envían datos mínimos
            // Para ser seguros, NO deberíamos enviar la respuesta correcta hasta que se cierre, pero esto es para el sondeo de estado
            
            if (question.getIsOpen()) {
                response.put("state", "ACTIVE");
                // Calcular tiempo restante
                long elapsed = java.time.Duration.between(question.getStartTime(), java.time.LocalDateTime.now()).getSeconds();
                long total = question.getRoom().getTimePerQuestion();
                response.put("remainingSeconds", Math.max(0, total - elapsed));
            } else {
                 response.put("state", "CLOSED");
            }
        } else {
            response.put("finished", false);
            response.put("state", "WAITING");
        }
        return response;
    }
}