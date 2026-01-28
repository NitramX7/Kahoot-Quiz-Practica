package com.quizlive.repository;

import com.quizlive.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByBlockId(Long blockId);

    long countByBlockId(Long blockId);

    void deleteByBlockId(Long blockId);

    List<Question> findByBlock_Owner_Id(Long userId);
}