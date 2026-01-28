package com.quizlive.controller;

import com.quizlive.dto.BlockDTO;
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

    // --- Endpoints de vista ---

    @GetMapping("/banco-preguntas")
    public String viewQuestionBank(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("username", user.getUsername());
        return "banco-preguntas";
    }

    // --- Endpoints REST ---

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
    public ResponseEntity<List<BlockDTO>> getUserBlocks(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        List<Block> blocks = blockService.getBlocksByUser(user.getId());
        List<BlockDTO> dtos = blocks.stream()
                .map(block -> new BlockDTO(block.getId(), block.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    // Ayuda
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