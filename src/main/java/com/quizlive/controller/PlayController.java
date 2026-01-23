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
            
            // Store player info in session for subsequent game steps
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
}
