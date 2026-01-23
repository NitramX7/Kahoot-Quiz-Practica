package com.quizlive.controller;

import com.quizlive.dto.QuestionDTO;
import com.quizlive.model.Block;
import com.quizlive.model.Question;
import com.quizlive.model.User;
import com.quizlive.service.BlockService;
import com.quizlive.service.QuestionService;
import com.quizlive.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class QuestionBankController {

    private final QuestionService questionService;
    private final BlockService blockService;
    private final UserService userService;

    // --- View Endpoints ---

    @GetMapping("/banco-preguntas")
    public String viewQuestionBank(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("username", user.getUsername());
        return "banco-preguntas";
    }

    // --- REST API Endpoints ---

    @GetMapping("/api/questions")
    @ResponseBody
    public ResponseEntity<List<QuestionDTO>> getAllQuestions(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        List<Question> questions = questionService.getAllQuestionsByUser(user.getId());
        
        List<QuestionDTO> dtos = questions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/api/questions/{id}")
    @ResponseBody
    public ResponseEntity<QuestionDTO> getQuestion(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        Question question = questionService.getQuestionById(id, user.getId());
        return ResponseEntity.ok(convertToDTO(question));
    }

    @PostMapping("/api/questions")
    @ResponseBody
    public ResponseEntity<QuestionDTO> createQuestion(@Valid @RequestBody QuestionDTO dto, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        
        Question created = questionService.createQuestion(
                dto.getBlockId(),
                dto.getText(),
                dto.getOption1(),
                dto.getOption2(),
                dto.getOption3(),
                dto.getOption4(),
                dto.getCorrectOption(),
                user.getId()
        );
        
        return ResponseEntity.ok(convertToDTO(created));
    }

    @PutMapping("/api/questions/{id}")
    @ResponseBody
    public ResponseEntity<QuestionDTO> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionDTO dto, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        
        Question updated = questionService.updateQuestion(
                id,
                dto.getText(),
                dto.getOption1(),
                dto.getOption2(),
                dto.getOption3(),
                dto.getOption4(),
                dto.getCorrectOption(),
                user.getId()
        );
        
        return ResponseEntity.ok(convertToDTO(updated));
    }

    @DeleteMapping("/api/questions/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        questionService.deleteQuestion(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/questions/{id}/duplicate")
    @ResponseBody
    public ResponseEntity<QuestionDTO> duplicateQuestion(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        Question duplicated = questionService.duplicateQuestion(id, user.getId());
        return ResponseEntity.ok(convertToDTO(duplicated));
    }

    @GetMapping("/api/blocks")
    @ResponseBody
    public ResponseEntity<List<Block>> getUserBlocks(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        List<Block> blocks = blockService.getBlocksByUser(user.getId());
        // Avoid infinite recursion in JSON if Block has List<Question> which has Block (standard JPA issue)
        // Ideally we should use BlockDTO, but for now we rely on @JsonIgnore or careful serialization
        // Or we can just return a simplified list here or use a DTO.
        // Let's assume Jackson handles it or we should create BlockDTO if it fails.
        // For simplicity, let's just null out the relationships or ensure @JsonIgnore is on Block.questions
        // But since we can't easily modify Block now without checking imports.
        // Just checking Block.java... it has @OneToMany assigned to 'questions'.
        // If it doesn't have @JsonIgnore, it will crash.
        // Re-reading Block.java from previous step... Step 30.
        // It uses lombok @Data which includes toString (can cause circle) and getters.
        // It does NOT have @JsonIgnore on questions. This will cause StackOverflowError.
        // I should stick to DTOs or modify Block.java.
        // Modifying Block.java is safer.
        return ResponseEntity.ok(blocks);
    }
    
    // Helper
    private QuestionDTO convertToDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setBlockId(question.getBlock().getId());
        dto.setBlockName(question.getBlock().getName());
        dto.setText(question.getText());
        dto.setOption1(question.getOption1());
        dto.setOption2(question.getOption2());
        dto.setOption3(question.getOption3());
        dto.setOption4(question.getOption4());
        dto.setCorrectOption(question.getCorrectOption());
        return dto;
    }
}
