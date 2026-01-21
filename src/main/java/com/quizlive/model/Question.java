package com.quizlive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Question entity with 4 options and correct answer validation
 */
@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "block_id", nullable = false)
    private Block block;

    @NotBlank(message = "Question text is required")
    @Column(nullable = false, length = 1000)
    private String text;

    @NotBlank(message = "Option 1 is required")
    @Column(nullable = false, length = 500)
    private String option1;

    @NotBlank(message = "Option 2 is required")
    @Column(nullable = false, length = 500)
    private String option2;

    @NotBlank(message = "Option 3 is required")
    @Column(nullable = false, length = 500)
    private String option3;

    @NotBlank(message = "Option 4 is required")
    @Column(nullable = false, length = 500)
    private String option4;

    @Min(value = 1, message = "Correct option must be between 1 and 4")
    @Max(value = 4, message = "Correct option must be between 1 and 4")
    @Column(name = "correct_option", nullable = false)
    private Integer correctOption;

    /**
     * Get the correct answer text based on correctOption
     */
    public String getCorrectAnswer() {
        return switch (correctOption) {
            case 1 -> option1;
            case 2 -> option2;
            case 3 -> option3;
            case 4 -> option4;
            default -> null;
        };
    }

    /**
     * Check if a given option number is correct
     */
    public boolean isCorrect(Integer selectedOption) {
        return correctOption.equals(selectedOption);
    }

    /**
     * Validate that all options are different (optional but recommended)
     */
    @PrePersist
    @PreUpdate
    public void validateOptions() {
        if (option1.equals(option2) || option1.equals(option3) || option1.equals(option4) ||
            option2.equals(option3) || option2.equals(option4) || option3.equals(option4)) {
            throw new IllegalStateException("All options must be different");
        }
    }
}
