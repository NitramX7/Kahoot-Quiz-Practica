package com.quizlive.controller;

import com.quizlive.model.Player;
import com.quizlive.model.Room;
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
}
