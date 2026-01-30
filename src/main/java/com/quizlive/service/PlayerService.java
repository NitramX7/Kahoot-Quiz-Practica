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
 * Servicio de gestión de jugadores
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;

    /**
 * Unirse a una sala (crear jugador)
 */
    @Transactional
    public Player joinRoom(String pin, String playerName) {
        Room room = roomRepository.findByPin(pin)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with PIN: " + pin));

        // Comprobar estado de la sala
        if (!room.isWaiting()) {
            throw new IllegalStateException("Room is not accepting new players");
        }

        // Comprobar nombre duplicado
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
 * Obtener todos los jugadores de una sala
 */
    public List<Player> getPlayersByRoom(Long roomId) {
        return playerRepository.findByRoomId(roomId);
    }

    /**
 * Obtener jugadores ordenados por puntuación (ranking)
 */
    public List<Player> getRankingByRoom(Long roomId) {
        return playerRepository.findByRoomIdOrderByScoreDesc(roomId);
    }

    /**
 * Buscar jugador por PIN y nombre
 */
    public Player getPlayerByPinAndName(String pin, String name) {
        return playerRepository.findByRoomPinAndName(pin, name)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }

    /**
     * Buscar jugador por ID
     */
    public Player getPlayerById(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }

    /**
     * Obtener jugadores de una sala por PIN
     * Útil para el monitoring controller
     */
    public List<Player> getPlayersByRoomPin(String pin) {
        Room room = roomRepository.findByPin(pin)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with PIN: " + pin));
        return playerRepository.findByRoomId(room.getId());
    }

    /**
     * Actualizar puntuación del jugador (thread-safe si se llama con sincronización adecuada)
     */
    @Transactional
    public void updatePlayerScore(Long playerId, int points) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        player.addScore(points);
        playerRepository.save(player);
    }
}