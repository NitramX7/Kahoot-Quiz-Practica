package com.quizlive.controller;

import com.quizlive.model.Block;
import com.quizlive.model.Room;
import com.quizlive.model.User;
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

import java.security.Principal;
import java.util.List;

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

    @GetMapping("/new")
    public String showCreateRoomForm(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName());
        List<Block> availableBlocks = blockService.getBlocksByUser(user.getId());
        
        if (availableBlocks.isEmpty()) {
            model.addAttribute("error", "Necesitas crear al menos un bloque para poder crear una sala.");
        }
        
        model.addAttribute("blocks", availableBlocks);
        model.addAttribute("user", user); // For the sidebar
        return "rooms/new";
    }

    @PostMapping
    public String createRoom(@RequestParam Long blockId,
                             @RequestParam Integer numQuestions,
                             @RequestParam Integer timePerQuestion,
                             @RequestParam String selectionMode,
                             Principal principal) {
        User host = userService.findByUsername(principal.getName());
        
        Room.SelectionMode mode = Room.SelectionMode.valueOf(selectionMode.toUpperCase());
        
        Room room = roomService.createRoom(blockId, numQuestions, mode, timePerQuestion, host, null);
        
        return "redirect:/rooms/" + room.getPin() + "/lobby";
    }

    @GetMapping("/{pin}/lobby")
    public String showLobby(@PathVariable String pin, Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName());
        Room room = roomService.getRoomByPin(pin);
        
        // Security check: only host can see the host lobby
        if (!room.getHost().getId().equals(user.getId())) {
            return "redirect:/blocks?error=access_denied";
        }
        
        model.addAttribute("room", room);
        model.addAttribute("players", playerService.getPlayersByRoom(room.getId()));
        model.addAttribute("user", user); // For the sidebar
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
        return "redirect:/rooms/" + id + "/game"; // This page doesn't exist yet, but the flow is correct
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
        // This should be the player view
        var room = roomService.getRoomByPin(pin);
        var player = playerService.getPlayerByPinAndName(pin, playerName);
        model.addAttribute("room", room);
        model.addAttribute("player", player);
        return "play/game/game";
    }

    @GetMapping("/{id}/podium")
    public String showPodium(@PathVariable Long id, Model model) {
        model.addAttribute("ranking", playerService.getRankingByRoom(id));
        return "rooms/podium";
    }
}
