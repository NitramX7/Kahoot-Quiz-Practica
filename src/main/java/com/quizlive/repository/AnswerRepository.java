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

    // Queries para estadísticas del podio
    @Query("SELECT a.roomQuestion, COUNT(a) FROM Answer a WHERE a.roomQuestion.room.id = :roomId AND a.isCorrect = false GROUP BY a.roomQuestion ORDER BY COUNT(a) DESC")
    List<Object[]> findMostFailedQuestionsByRoom(@Param("roomId") Long roomId);

    @Query("SELECT a.roomQuestion, COUNT(a) FROM Answer a WHERE a.roomQuestion.room.id = :roomId AND a.isCorrect = true GROUP BY a.roomQuestion ORDER BY COUNT(a) DESC")
    List<Object[]> findMostCorrectQuestionsByRoom(@Param("roomId") Long roomId);

    @Query("SELECT a FROM Answer a WHERE a.player.id = :playerId AND a.roomQuestion.room.id = :roomId ORDER BY a.roomQuestion.orderNum")
    List<Answer> findPlayerAnswersInRoom(@Param("playerId") Long playerId, @Param("roomId") Long roomId);
}