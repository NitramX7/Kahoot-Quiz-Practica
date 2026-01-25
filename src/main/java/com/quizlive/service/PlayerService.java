package com.quizlive.service;

import com.quizlive.model.Player;
import com.quizlive.model.Room;
import com.quizlive.repository.PlayerRepository;
import com.quizlive.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for Player management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;

    /**
     * Join a room (create player)
     */
    @Transactional
    public Player joinRoom(String pin, String playerName) {
        Room room = roomRepository.findByPin(pin)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with PIN: " + pin));

        // Check room state
        if (!room.isWaiting()) {
            throw new IllegalStateException("Room is not accepting new players");
        }

        // Check duplicate name
        if (playerRepository.existsByRoomIdAndName(room.getId(), playerName)) {
            throw new IllegalArgumentException("Player name already exists in this room");
        }

        Player player = new Player();
        player.setRoom(room);
        player.setName(playerName);
        player.setScore(0);

        Player saved = playerRepository.save(player);
        log.info("[Room {}] Player {} joined", pin, playerName);
        return saved;
    }

    /**
     * Get all players in a room
     */
    public List<Player> getPlayersByRoom(Long roomId) {
        return playerRepository.findByRoomId(roomId);
    }

    /**
     * Get players sorted by score (ranking)
     */
    public List<Player> getRankingByRoom(Long roomId) {
        return playerRepository.findByRoomIdOrderByScoreDesc(roomId);
    }

    /**
     * Find player by PIN and name
     */
    public Player getPlayerByPinAndName(String pin, String name) {
        return playerRepository.findByRoomPinAndName(pin, name)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }

    /**
     * Find player by ID
     */
    public Player getPlayerById(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }

    /**
     * Update player score (thread-safe when called with proper synchronization)
     */
    @Transactional
    public void updatePlayerScore(Long playerId, int points) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        player.addScore(points);
        playerRepository.save(player);
    }
}
