package com.quizlive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight block DTO for dropdowns and filters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockDTO {
    private Long id;
    private String name;
}
