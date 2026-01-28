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
 * Servicio de gestión de preguntas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final BlockService blockService;

    /**
 * Crear una nueva pregunta en un bloque
 */
    @Transactional
    public Question createQuestion(Long blockId, String text, String option1, String option2,
                                   String option3, String option4, Integer correctOption, Long userId) {
        // Validar propiedad del bloque
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
 * Obtener todas las preguntas de un bloque
 */
    public List<Question> getQuestionsByBlock(Long blockId, Long userId) {
        // Validar propiedad
        blockService.getBlockById(blockId, userId);
        return questionRepository.findByBlockId(blockId);
    }

    /**
 * Obtener pregunta por ID con validación de propiedad
 */
    public Question getQuestionById(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        
        // Validar propiedad a través del bloque
        blockService.getBlockById(question.getBlock().getId(), userId);
        
        return question;
    }

    /**
 * Actualizar pregunta
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
 * Eliminar pregunta
 */
    @Transactional
    public void deleteQuestion(Long questionId, Long userId) {
        Question question = getQuestionById(questionId, userId);
        questionRepository.delete(question);
        log.info("Deleted question {}", questionId);
    }

    /**
 * Contar preguntas en un bloque
 */
    public long countQuestionsByBlock(Long blockId) {
        return questionRepository.countByBlockId(blockId);
    }

    /**
 * Obtener todas las preguntas de un usuario en todos sus bloques
 */
    public List<Question> getAllQuestionsByUser(Long userId) {
        return questionRepository.findByBlock_Owner_Id(userId);
    }

    /**
 * Duplicar una pregunta
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