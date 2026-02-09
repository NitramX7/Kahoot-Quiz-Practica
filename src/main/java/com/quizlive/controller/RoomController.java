package com.quizlive.controller;

import com.quizlive.model.Answer;
import com.quizlive.model.Block;
import com.quizlive.model.Player;
import com.quizlive.model.Room;
import com.quizlive.model.RoomQuestion;
import com.quizlive.model.User;
import com.quizlive.repository.AnswerRepository;
import com.quizlive.service.BlockService;
import com.quizlive.service.GameEngineService;
import com.quizlive.service.PlayerService;
import com.quizlive.service.RoomService;
import com.quizlive.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomController {

    private final RoomService roomService;
    private final BlockService blockService;
    private final UserService userService;
    private final PlayerService playerService;
    private final GameEngineService gameEngineService;
    private final AnswerRepository answerRepository;

    @GetMapping("/new")
    public String showCreateRoomForm(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName());
        List<Block> availableBlocks = blockService.getBlocksByUser(user.getId());
        
        if (availableBlocks.isEmpty()) {
            model.addAttribute("error", "Necesitas crear al menos un bloque para poder crear una sala.");
        }
        
        model.addAttribute("blocks", availableBlocks);
        model.addAttribute("user", user); // Para la barra lateral
        return "rooms/new";
    }

    @PostMapping
    public String createRoom(@RequestParam Long blockId,
                             @RequestParam Integer numQuestions,
                             @RequestParam Integer timePerQuestion,
                             @RequestParam String selectionMode,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        User host = userService.findByUsername(principal.getName());
        
        try {
            Room.SelectionMode mode = Room.SelectionMode.valueOf(selectionMode.toUpperCase());
            
            Room room = roomService.createRoom(blockId, numQuestions, mode, timePerQuestion, host, null);
            
            return "redirect:/rooms/" + room.getPin() + "/lobby";
        } catch (IllegalArgumentException e) {
            // Capturar error cuando hay más preguntas solicitadas que disponibles
            log.warn("Error creating room: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", 
                "El bloque seleccionado no tiene suficientes preguntas. Por favor, elige un número menor o selecciona otro bloque.");
            return "redirect:/rooms/new";
        }
    }

    @GetMapping("/{pin}/lobby")
    public String showLobby(@PathVariable String pin, Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName());
        Room room = roomService.getRoomByPin(pin);
        
        // Comprobación de seguridad: solo el anfitrión puede ver el lobby del anfitrión
        if (!room.getHost().getId().equals(user.getId())) {
            return "redirect:/blocks?error=access_denied";
        }
        
        model.addAttribute("room", room);
        model.addAttribute("players", playerService.getPlayersByRoom(room.getId()));
        model.addAttribute("user", user); // Para la barra lateral
        return "rooms/lobby";
    }

    @PostMapping("/{id}/start")
    public String startRoom(@PathVariable Long id, Principal principal) {
        User host = userService.findByUsername(principal.getName());
        Room room = roomService.getRoomById(id);
        if (!room.getHost().getId().equals(host.getId())) {
            throw new SecurityException("Only the host can start the room");
        }
        gameEngineService.startGame(room.getPin());
        return "redirect:/rooms/" + id + "/game"; // Esta página aún no existe, pero el flujo es correcto
    }
    @GetMapping("/{id}/game")
    public String showGame(@PathVariable Long id, Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName());
        Room room = roomService.getRoomById(id);
        model.addAttribute("room", room);
        try {
            var q = gameEngineService.getCurrentQuestion(room.getPin());
            model.addAttribute("currentQuestion", q);
        } catch (Exception e) {
            log.warn("Could not fetch current question for room {}: {}", room.getPin(), e.getMessage());
        }
        return "rooms/game_host";
    }

    @GetMapping("/{pin}/play")
    public String showPlayerGame(@PathVariable String pin, @RequestParam String playerName, Model model) {
        // Esta debería ser la vista del jugador
        var room = roomService.getRoomByPin(pin);
        var player = playerService.getPlayerByPinAndName(pin, playerName);
        model.addAttribute("room", room);
        model.addAttribute("player", player);
        return "play/game/game";
    }

    @GetMapping("/{id}/podium")
    public String showPodium(@PathVariable Long id, 
                            @RequestParam(required = false) String playerName,
                            Model model) {
        // Ranking de jugadores
        List<Player> ranking = playerService.getRankingByRoom(id);
        model.addAttribute("ranking", ranking);
        
        // Solo buscar estadísticas personales si hay un playerName en la URL
        Long currentPlayerId = null;
        if (playerName != null && !playerName.isEmpty()) {
            Player currentPlayer = ranking.stream()
                .filter(p -> p.getName().equalsIgnoreCase(playerName))
                .findFirst()
                .orElse(null);
            if (currentPlayer != null) {
                currentPlayerId = currentPlayer.getId();
            }
        }
        
        // Estadísticas globales: pregunta más fallada
        List<Object[]> failedQuestions = answerRepository.findMostFailedQuestionsByRoom(id);
        if (!failedQuestions.isEmpty()) {
            Object[] mostFailed = failedQuestions.get(0);
            RoomQuestion roomQuestion = (RoomQuestion) mostFailed[0];
            Long failCount = (Long) mostFailed[1];
            model.addAttribute("mostFailedQuestion", roomQuestion.getQuestion().getText());
            model.addAttribute("mostFailedCount", failCount);
        }
        
        // Estadísticas globales: pregunta más acertada
        List<Object[]> correctQuestions = answerRepository.findMostCorrectQuestionsByRoom(id);
        if (!correctQuestions.isEmpty()) {
            Object[] mostCorrect = correctQuestions.get(0);
            RoomQuestion roomQuestion = (RoomQuestion) mostCorrect[0];
            Long correctCount = (Long) mostCorrect[1];
            model.addAttribute("mostCorrectQuestion", roomQuestion.getQuestion().getText());
            model.addAttribute("mostCorrectCount", correctCount);
        }
        
        // Calcular tasa global de aciertos
        List<Answer> allAnswers = answerRepository.findByRoomId(id);
        if (!allAnswers.isEmpty()) {
            long totalCorrect = allAnswers.stream().filter(Answer::getIsCorrect).count();
            double successRate = (double) totalCorrect / allAnswers.size() * 100;
            model.addAttribute("globalSuccessRate", String.format("%.1f", successRate));
        }
        
        // Estadísticas personales del jugador actual (solo si playerName está presente)
        if (currentPlayerId != null) {
            List<Answer> playerAnswers = answerRepository.findPlayerAnswersInRoom(currentPlayerId, id);
            if (!playerAnswers.isEmpty()) {
                long correct = playerAnswers.stream().filter(Answer::getIsCorrect).count();
                long incorrect = playerAnswers.size() - correct;
                double personalRate = (double) correct / playerAnswers.size() * 100;
                
                // Mejor tiempo de respuesta
                Long bestTime = playerAnswers.stream()
                    .filter(a -> a.getResponseTime() != null)
                    .map(Answer::getResponseTime)
                    .min(Long::compareTo)
                    .orElse(null);
                
                model.addAttribute("playerCorrect", correct);
                model.addAttribute("playerIncorrect", incorrect);
                model.addAttribute("playerSuccessRate", String.format("%.1f", personalRate));
                model.addAttribute("playerName", playerName); // Para mostrar "Tu Rendimiento ({nombre})"
                if (bestTime != null) {
                    double bestTimeSeconds = bestTime / 1000.0;
                    model.addAttribute("playerBestTime", String.format("%.2f", bestTimeSeconds));
                }
            }
        }
        
        return "rooms/podium";
    }
}