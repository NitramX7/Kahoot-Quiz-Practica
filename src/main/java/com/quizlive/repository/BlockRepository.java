package com.quizlive.repository;

import com.quizlive.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    List<Block> findByOwnerId(Long userId);

    @Query("SELECT COUNT(b) > 0 FROM Block b WHERE b.id = :blockId AND b.owner.id = :userId")
    boolean isOwnedByUser(@Param("blockId") Long blockId, @Param("userId") Long userId);
}