package com.quizlive.controller;

import com.quizlive.model.Block;
import com.quizlive.model.Room;
import com.quizlive.model.User;
import com.quizlive.service.BlockService;
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

    @GetMapping("/new")
    public String showCreateRoomForm(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName());
        List<Block> availableBlocks = blockService.getBlocksWithMinimumQuestions(user.getId(), 20);
        
        if (availableBlocks.isEmpty()) {
            model.addAttribute("error", "Necesitas tener al menos un bloque con 20 o más preguntas para crear una sala.");
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
        roomService.startRoom(id, host.getId());
        return "redirect:/rooms/" + id + "/game"; // This page doesn't exist yet, but the flow is correct
    }
}
