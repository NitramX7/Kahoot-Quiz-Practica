package com.quizlive.service;

import com.quizlive.model.Block;
import com.quizlive.model.Question;
import com.quizlive.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for Question management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final BlockService blockService;

    /**
     * Create a new question in a block
     */
    @Transactional
    public Question createQuestion(Long blockId, String text, String option1, String option2,
                                   String option3, String option4, Integer correctOption, Long userId) {
        // Validate block ownership
        Block block = blockService.getBlockById(blockId, userId);

        Question question = new Question();
        question.setBlock(block);
        question.setText(text);
        question.setOption1(option1);
        question.setOption2(option2);
        question.setOption3(option3);
        question.setOption4(option4);
        question.setCorrectOption(correctOption);

        Question saved = questionRepository.save(question);
        log.info("Created question in block {}", blockId);
        return saved;
    }

    /**
     * Get all questions in a block
     */
    public List<Question> getQuestionsByBlock(Long blockId, Long userId) {
        // Validate ownership
        blockService.getBlockById(blockId, userId);
        return questionRepository.findByBlockId(blockId);
    }

    /**
     * Get question by ID with ownership validation
     */
    public Question getQuestionById(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        
        // Validate ownership through block
        blockService.getBlockById(question.getBlock().getId(), userId);
        
        return question;
    }

    /**
     * Update question
     */
    @Transactional
    public Question updateQuestion(Long questionId, String text, String option1, String option2,
                                   String option3, String option4, Integer correctOption, Long userId) {
        Question question = getQuestionById(questionId, userId);
        
        question.setText(text);
        question.setOption1(option1);
        question.setOption2(option2);
        question.setOption3(option3);
        question.setOption4(option4);
        question.setCorrectOption(correctOption);

        log.info("Updated question {}", questionId);
        return questionRepository.save(question);
    }

    /**
     * Delete question
     */
    @Transactional
    public void deleteQuestion(Long questionId, Long userId) {
        Question question = getQuestionById(questionId, userId);
        questionRepository.delete(question);
        log.info("Deleted question {}", questionId);
    }

    /**
     * Count questions in a block
     */
    public long countQuestionsByBlock(Long blockId) {
        return questionRepository.countByBlockId(blockId);
    }

    /**
     * Get all questions for a specific user across all blocks
     */
    public List<Question> getAllQuestionsByUser(Long userId) {
        return questionRepository.findByBlock_Owner_Id(userId);
    }

    /**
     * Duplicate a question
     */
    @Transactional
    public Question duplicateQuestion(Long questionId, Long userId) {
        Question original = getQuestionById(questionId, userId);
        
        Question copy = new Question();
        copy.setBlock(original.getBlock());
        copy.setText(original.getText() + " (Copia)");
        copy.setOption1(original.getOption1());
        copy.setOption2(original.getOption2());
        copy.setOption3(original.getOption3());
        copy.setOption4(original.getOption4());
        copy.setCorrectOption(original.getCorrectOption());
        
        log.info("Duplicated question {} -> {}", questionId, copy.getText());
        return questionRepository.save(copy);
    }
}
