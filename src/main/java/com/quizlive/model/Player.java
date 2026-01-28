package com.quizlive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Player que representa un participante en una sala
 */
@Entity
@Table(name = "players")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Room room;

    @NotBlank(message = "Player name is required")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer score = 0;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    // Relaciones
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    /**
 * Añadir puntos a la puntuación del jugador (thread-safe con sincronización adecuada)
 */
    public void addScore(int points) {
        this.score += points;
    }

    /**
 * Comprobar si el jugador ya ha respondido una pregunta específica
 */
    public boolean hasAnsweredQuestion(Long roomQuestionId) {
        return answers.stream()
                .anyMatch(answer -> answer.getRoomQuestion().getId().equals(roomQuestionId));
    }
}