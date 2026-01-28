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
 * Entidad Answer que almacena respuestas del jugador con tiempo y corrección
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
    private Long responseTime; // Tiempo en milisegundos desde el inicio de la pregunta

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned = 0;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    /**
 * Calcular puntos obtenidos según corrección y bono opcional de velocidad
 * Básico: +1 por correcta, 0 por incorrecta
 * Opcional: puntos extra por respuestas más rápidas
 */
    public void calculatePoints(boolean useSpeedBonus, int maxTime) {
        if (!isCorrect) {
            this.pointsEarned = 0;
            return;
        }

        // Puntos base por respuesta correcta
        int points = 1;

        // Bono opcional de velocidad: puntos extra por respuestas rápidas
        if (useSpeedBonus && responseTime != null) {
            double timeRatio = (double) responseTime / (maxTime * 1000); // Convertir a segundos
            if (timeRatio < 0.25) {
                points += 3; // Muy rápido: +3 de bonificación
            } else if (timeRatio < 0.5) {
                points += 2; // Rápido: +2 de bonificación
            } else if (timeRatio < 0.75) {
                points += 1; // Medio: +1 de bonificación
            }
            // Lento: sin bonificación
        }

        this.pointsEarned = points;
    }
}