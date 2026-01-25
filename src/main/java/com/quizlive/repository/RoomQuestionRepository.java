package com.quizlive.repository;

import com.quizlive.model.RoomQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for RoomQuestion entity
 */
@Repository
public interface RoomQuestionRepository extends JpaRepository<RoomQuestion, Long> {

    /**
     * Find all questions for a room, ordered by orderNum
     */
    List<RoomQuestion> findByRoomIdOrderByOrderNumAsc(Long roomId);

    /**
     * Find currently open question in a room
     */
    Optional<RoomQuestion> findByRoomIdAndIsOpenTrue(Long roomId);

    /**
     * Find question by room and order number
     */
    Optional<RoomQuestion> findByRoomIdAndOrderNum(Long roomId, Integer orderNum);

    /**
     * Count total questions in a room
     */
    long countByRoomId(Long roomId);

    /**
     * Find next unopened question
     */
    @Query("SELECT rq FROM RoomQuestion rq WHERE rq.room.id = :roomId AND rq.startTime IS NULL ORDER BY rq.orderNum ASC")
    List<RoomQuestion> findUnopenedQuestionsByRoomId(@Param("roomId") Long roomId);

    /**
     * Find a question with its Question entity eagerly loaded
     */
    @Query("SELECT rq FROM RoomQuestion rq JOIN FETCH rq.question WHERE rq.id = :id")
    Optional<RoomQuestion> findByIdWithQuestion(@Param("id") Long id);
}
