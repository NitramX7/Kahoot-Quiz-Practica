package com.quizlive.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    
    private Long id;
    
    @NotNull(message = "Block ID is required")
    private Long blockId;
    
    private String blockName; // Para mostrar
    
    @NotBlank(message = "Question text is required")
    private String text;
    
    @NotBlank(message = "Option 1 is required")
    private String option1;
    
    @NotBlank(message = "Option 2 is required")
    private String option2;
    
    @NotBlank(message = "Option 3 is required")
    private String option3;
    
    @NotBlank(message = "Option 4 is required")
    private String option4;
    
    @NotNull(message = "Correct option is required")
    @Min(value = 1, message = "Correct option must be between 1 and 4")
    @Max(value = 4, message = "Correct option must be between 1 and 4")
    private Integer correctOption;
}