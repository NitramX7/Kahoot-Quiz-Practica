package com.quizlive.repository;

import com.quizlive.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Question entity
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Find all questions in a specific block
     */
    List<Question> findByBlockId(Long blockId);

    /**
     * Count questions in a block
     */
    long countByBlockId(Long blockId);

    /**
     * Delete all questions in a block
     */
    void deleteByBlockId(Long blockId);
}
