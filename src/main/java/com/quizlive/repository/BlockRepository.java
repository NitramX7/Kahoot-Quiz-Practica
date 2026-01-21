package com.quizlive.repository;

import com.quizlive.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Block entity
 */
@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    /**
     * Find all blocks owned by a specific user
     */
    List<Block> findByOwnerId(Long userId);

    /**
     * Find blocks by user ID with at least minimum questions
     */
    @Query("SELECT b FROM Block b WHERE b.owner.id = :userId AND SIZE(b.questions) >= :minQuestions")
    List<Block> findByOwnerIdWithMinimumQuestions(@Param("userId") Long userId, 
                                                    @Param("minQuestions") int minQuestions);

    /**
     * Check if a specific user owns a specific block
     */
    @Query("SELECT COUNT(b) > 0 FROM Block b WHERE b.id = :blockId AND b.owner.id = :userId")
    boolean isOwnedByUser(@Param("blockId") Long blockId, @Param("userId") Long userId);
}
