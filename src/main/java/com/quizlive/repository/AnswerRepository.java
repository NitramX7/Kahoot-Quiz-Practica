package com.quizlive.repository;

import com.quizlive.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByPlayerId(Long playerId);

    Optional<Answer> findByPlayerIdAndRoomQuestionId(Long playerId, Long roomQuestionId);

    boolean existsByPlayerIdAndRoomQuestionId(Long playerId, Long roomQuestionId);

    List<Answer> findByRoomQuestionId(Long roomQuestionId);

    @Query("SELECT COUNT(a) FROM Answer a WHERE a.roomQuestion.id = :roomQuestionId AND a.isCorrect = true")
    long countCorrectAnswersByRoomQuestion(@Param("roomQuestionId") Long roomQuestionId);

    long countByRoomQuestionId(Long roomQuestionId);

    @Query("SELECT a FROM Answer a WHERE a.roomQuestion.room.id = :roomId")
    List<Answer> findByRoomId(@Param("roomId") Long roomId);
}