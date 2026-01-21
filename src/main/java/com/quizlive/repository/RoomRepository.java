package com.quizlive.repository;

import com.quizlive.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Room entity
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Find room by PIN
     */
    Optional<Room> findByPin(String pin);

    /**
     * Find all rooms created by a specific host
     */
    List<Room> findByHostId(Long hostId);

    /**
     * Find rooms by state
     */
    List<Room> findByState(Room.RoomState state);

    /**
     * Check if PIN already exists
     */
    boolean existsByPin(String pin);

    /**
     * Find all active rooms (WAITING or RUNNING)
     */
    List<Room> findByStateIn(List<Room.RoomState> states);
}
