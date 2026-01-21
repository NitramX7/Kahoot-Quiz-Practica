package com.quizlive.repository;

import com.quizlive.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Player entity
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    /**
     * Find all players in a room
     */
    List<Player> findByRoomId(Long roomId);

    /**
     * Find all players in a room ordered by score (descending)
     */
    List<Player> findByRoomIdOrderByScoreDesc(Long roomId);

    /**
     * Find player by room and name
     */
    Optional<Player> findByRoomIdAndName(Long roomId, String name);

    /**
     * Check if player name exists in room
     */
    boolean existsByRoomIdAndName(Long roomId, String name);

    /**
     * Count players in a room
     */
    long countByRoomId(Long roomId);

    /**
     * Find player by room PIN and name
     */
    @Query("SELECT p FROM Player p WHERE p.room.pin = :pin AND p.name = :name")
    Optional<Player> findByRoomPinAndName(@Param("pin") String pin, @Param("name") String name);
}
