package com.quizlive.repository;

import com.quizlive.model.RoomQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomQuestionRepository extends JpaRepository<RoomQuestion, Long> {

    List<RoomQuestion> findByRoomIdOrderByOrderNumAsc(Long roomId);

    Optional<RoomQuestion> findByRoomIdAndIsOpenTrue(Long roomId);

    Optional<RoomQuestion> findByRoomIdAndOrderNum(Long roomId, Integer orderNum);

    long countByRoomId(Long roomId);

    @Query("SELECT rq FROM RoomQuestion rq WHERE rq.room.id = :roomId AND rq.startTime IS NULL ORDER BY rq.orderNum ASC")
    List<RoomQuestion> findUnopenedQuestionsByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT rq FROM RoomQuestion rq JOIN FETCH rq.question WHERE rq.id = :id")
    Optional<RoomQuestion> findByIdWithQuestion(@Param("id") Long id);
}