package com.quizlive.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RoomQuestion entity linking rooms to selected questions
 * Maintains order and open/closed state for each question in the game
 */
@Entity
@Table(name = "room_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "order_num", nullable = false)
    private Integer orderNum; // Order in which this question appears (1, 2, 3...)

    @Column(name = "is_open", nullable = false)
    private Boolean isOpen = false; // Whether players can currently answer this question

    @Column(name = "start_time")
    private LocalDateTime startTime; // When this question was opened

    @Column(name = "close_time")
    private LocalDateTime closeTime; // When this question was closed

    /**
     * Open the question for answers
     */
    public void open() {
        this.isOpen = true;
        this.startTime = LocalDateTime.now();
    }

    /**
     * Close the question (time expired or all players answered)
     */
    public void close() {
        this.isOpen = false;
        this.closeTime = LocalDateTime.now();
    }

    /**
     * Check if question is still accepting answers
     */
    public boolean canAcceptAnswers() {
        return isOpen && startTime != null && closeTime == null;
    }
}
