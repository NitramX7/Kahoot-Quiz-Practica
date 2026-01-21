package com.quizlive.repository;

import com.quizlive.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Answer entity
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    /**
     * Find all answers by a player
     */
    List<Answer> findByPlayerId(Long playerId);

    /**
     * Find answer by player and room question
     */
    Optional<Answer> findByPlayerIdAndRoomQuestionId(Long playerId, Long roomQuestionId);

    /**
     * Check if player has already answered a question
     */
    boolean existsByPlayerIdAndRoomQuestionId(Long playerId, Long roomQuestionId);

    /**
     * Find all answers for a specific room question
     */
    List<Answer> findByRoomQuestionId(Long roomQuestionId);

    /**
     * Count correct answers for a room question
     */
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.roomQuestion.id = :roomQuestionId AND a.isCorrect = true")
    long countCorrectAnswersByRoomQuestion(@Param("roomQuestionId") Long roomQuestionId);

    /**
     * Count total answers for a room question
     */
    long countByRoomQuestionId(Long roomQuestionId);

    /**
     * Find all answers for a room (across all questions)
     */
    @Query("SELECT a FROM Answer a WHERE a.roomQuestion.room.id = :roomId")
    List<Answer> findByRoomId(@Param("roomId") Long roomId);
}
