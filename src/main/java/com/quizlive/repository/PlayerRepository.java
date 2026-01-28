package com.quizlive.repository;

import com.quizlive.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByRoomId(Long roomId);

    List<Player> findByRoomIdOrderByScoreDesc(Long roomId);

    Optional<Player> findByRoomIdAndName(Long roomId, String name);

    boolean existsByRoomIdAndName(Long roomId, String name);

    long countByRoomId(Long roomId);

    @Query("SELECT p FROM Player p WHERE p.room.pin = :pin AND p.name = :name")
    Optional<Player> findByRoomPinAndName(@Param("pin") String pin, @Param("name") String name);
}