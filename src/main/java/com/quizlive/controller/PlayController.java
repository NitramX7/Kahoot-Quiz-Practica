package com.quizlive.controller;

import com.quizlive.model.Player;
import com.quizlive.model.Room;
import com.quizlive.service.PlayerService;
import com.quizlive.service.RoomService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/play")
@RequiredArgsConstructor
@Slf4j
public class PlayController {

    private final RoomService roomService;
    private final PlayerService playerService;

    @GetMapping("/join")
    public String showJoinForm(@RequestParam(required = false) String pin, Model model) {
        model.addAttribute("pin", pin);
        return "play/join";
    }

    @PostMapping("/join")
    public String joinRoom(@RequestParam String pin, 
                           @RequestParam String playerName,
                           HttpSession session,
                           Model model) {
        try {
            Player player = playerService.joinRoom(pin, playerName);
            
            // Guardar info del jugador en sesión para pasos posteriores del juego
            session.setAttribute("playerPin", pin);
            session.setAttribute("playerName", playerName);
            session.setAttribute("playerId", player.getId());
            
            return "redirect:/play/wait";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pin", pin);
            model.addAttribute("playerName", playerName);
            return "play/join";
        }
    }

    @GetMapping("/wait")
    public String showWaitRoom(HttpSession session, Model model) {
        String pin = (String) session.getAttribute("playerPin");
        String name = (String) session.getAttribute("playerName");
        
        if (pin == null || name == null) {
            return "redirect:/play/join";
        }
        
        try {
            Room room = roomService.getRoomByPin(pin);
            model.addAttribute("room", room);
            model.addAttribute("playerName", name);
            
            if (room.isRunning()) {
                return "redirect:/play/game";
            }
            
            return "play/wait";
        } catch (Exception e) {
            return "redirect:/play/join?error=room_not_found";
        }
    }
    @GetMapping("/game")
    public String showGame(HttpSession session, Model model) {
        String pin = (String) session.getAttribute("playerPin");
        String name = (String) session.getAttribute("playerName");
        
        if (pin == null || name == null) {
            return "redirect:/play/join";
        }
        
        Room room = roomService.getRoomByPin(pin);
        Player player = playerService.getPlayerByPinAndName(pin, name);
        
        model.addAttribute("room", room);
        model.addAttribute("player", player);
        
        // Inyectar la pregunta actual para el renderizado inicial
        try {
             com.quizlive.service.GameEngineService gameEngineService = applicationContext.getBean(com.quizlive.service.GameEngineService.class);
             com.quizlive.model.RoomQuestion currentQuestion = gameEngineService.getCurrentQuestion(pin);
             if (currentQuestion != null) {
                 model.addAttribute("currentQuestion", currentQuestion);
             }
        } catch (Exception e) {
            log.warn("Could not fetch current question for room {}: {}", pin, e.getMessage());
        }
        
        return "play/game/game";
    }

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.context.ApplicationContext applicationContext;
}