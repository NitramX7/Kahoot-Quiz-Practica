package com.quizlive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Answer entity storing player responses with timing and correctness
 */
@Entity
@Table(name = "answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_question_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private RoomQuestion roomQuestion;

    @Min(value = 1, message = "Selected option must be between 1 and 4")
    @Max(value = 4, message = "Selected option must be between 1 and 4")
    @Column(name = "selected_option", nullable = false)
    private Integer selectedOption;

    @Column(name = "response_time", nullable = false)
    private Long responseTime; // Time in milliseconds from question start

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned = 0;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    /**
     * Calculate points earned based on correctness and optional speed bonus
     * Basic: +1 for correct, 0 for incorrect
     * Optional: bonus points for faster answers
     */
    public void calculatePoints(boolean useSpeedBonus, int maxTime) {
        if (!isCorrect) {
            this.pointsEarned = 0;
            return;
        }

        // Base points for correct answer
        int points = 1;

        // Optional speed bonus: extra points for quick answers
        if (useSpeedBonus && responseTime != null) {
            double timeRatio = (double) responseTime / (maxTime * 1000); // Convert to seconds
            if (timeRatio < 0.25) {
                points += 3; // Very fast: +3 bonus
            } else if (timeRatio < 0.5) {
                points += 2; // Fast: +2 bonus
            } else if (timeRatio < 0.75) {
                points += 1; // Medium: +1 bonus
            }
            // Slow: no bonus
        }

        this.pointsEarned = points;
    }
}
