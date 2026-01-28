package com.quizlive.repository;

import com.quizlive.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByPin(String pin);

    List<Room> findByHostId(Long hostId);

    List<Room> findByState(Room.RoomState state);

    boolean existsByPin(String pin);

    List<Room> findByStateIn(List<Room.RoomState> states);
}