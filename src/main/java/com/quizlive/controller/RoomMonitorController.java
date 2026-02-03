package com.quizlive.controller;

import com.quizlive.model.Player;
import com.quizlive.model.RoomQuestion;
import com.quizlive.service.GameEngineService;
import com.quizlive.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador REST para monitorear salas activas y estadísticas del sistema
 * PSP - Permite verificar el estado de concurrencia en tiempo real
 */
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
@Slf4j
public class RoomMonitorController {

    private final GameEngineService gameEngineService;
    private final PlayerService playerService;

    /**
     * GET /api/monitor/active-rooms
     * Listar todas las salas activas con información resumida
     */
    @GetMapping("/active-rooms")
    public ResponseEntity<Map<String, Object>> getActiveRooms() {
        log.debug("📊 [MONITOR] Solicitando lista de salas activas");
        
        Set<String> activePins = gameEngineService.getActiveRoomPins();
        
        List<Map<String, Object>> roomsInfo = activePins.stream()
                .map(pin -> {
                    try {
                        RoomQuestion currentQuestion = gameEngineService.getCurrentQuestion(pin);
                        List<Player> players = playerService.getPlayersByRoomPin(pin);
                        
                        Map<String, Object> roomInfo = new HashMap<>();
                        roomInfo.put("pin", pin);
                        roomInfo.put("activePlayers", players.size());
                        roomInfo.put("playerNames", players.stream()
                                .map(Player::getName)
                                .collect(Collectors.toList()));
                        
                        if (currentQuestion != null) {
                            roomInfo.put("currentQuestionId", currentQuestion.getId());
                            roomInfo.put("questionOrder", currentQuestion.getOrderNum());
                            roomInfo.put("questionOpen", currentQuestion.getIsOpen());
                        } else {
                            roomInfo.put("currentQuestionId", null);
                            roomInfo.put("questionOrder", "Finalizado");
                            roomInfo.put("questionOpen", false);
                        }
                        
                        return roomInfo;
                    } catch (Exception e) {
                        log.warn("⚠ Error obteniendo info de sala {}: {}", pin, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        response.put("activeRooms", activePins.size());
        response.put("rooms", roomsInfo);
        
        log.info("📊 [MONITOR] Devolviendo {} salas activas", activePins.size());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/monitor/room/{pin}
     * Obtener detalles específicos de una sala
     */
    @GetMapping("/room/{pin}")
    public ResponseEntity<Map<String, Object>> getRoomDetails(@PathVariable String pin) {
        log.debug("📊 [MONITOR] Solicitando detalles de sala {}", pin);
        
        if (!gameEngineService.isRoomActive(pin)) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            RoomQuestion currentQuestion = gameEngineService.getCurrentQuestion(pin);
            List<Player> ranking = gameEngineService.getRanking(pin);
            
            Map<String, Object> response = new HashMap<>();
            response.put("pin", pin);
            response.put("isActive", true);
            response.put("totalPlayers", ranking.size());
            
            if (currentQuestion != null) {
                response.put("currentQuestionId", currentQuestion.getId());
                response.put("questionOrder", currentQuestion.getOrderNum());
                response.put("questionText", currentQuestion.getQuestion().getText());
                response.put("questionOpen", currentQuestion.getIsOpen());
            }
            
            List<Map<String, Object>> playerRanking = ranking.stream()
                    .map(player -> {
                        Map<String, Object> playerInfo = new HashMap<>();
                        playerInfo.put("name", player.getName());
                        playerInfo.put("score", player.getScore());
                        return playerInfo;
                    })
                    .collect(Collectors.toList());
            
            response.put("ranking", playerRanking);
            
            log.info("📊 [MONITOR] Detalles de sala {} enviados", pin);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ [MONITOR] Error obteniendo detalles de sala {}: {}", pin, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/monitor/stats
     * Estadísticas generales del sistema
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        log.debug("📊 [MONITOR] Solicitando estadísticas del sistema");
        
        Set<String> activePins = gameEngineService.getActiveRoomPins();
        
        int totalPlayers = activePins.stream()
                .mapToInt(pin -> {
                    try {
                        return playerService.getPlayersByRoomPin(pin).size();
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("timestamp", System.currentTimeMillis());
        stats.put("activeRooms", activePins.size());
        stats.put("totalPlayers", totalPlayers);
        stats.put("systemStatus", "RUNNING");
        stats.put("concurrencyEnabled", true);
        
        // Información de thread pools (estimado)
        Map<String, Object> threadPools = new HashMap<>();
        threadPools.put("answerProcessingPoolSize", 10);
        threadPools.put("timerPoolSize", 10);
        stats.put("threadPools", threadPools);
        
        log.info("📊 [MONITOR] Estadísticas: {} salas, {} jugadores", 
                activePins.size(), totalPlayers);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * GET /api/monitor/health
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Quiz Live PSP");
        health.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(health);
    }
}
