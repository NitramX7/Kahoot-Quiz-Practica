package com.quizlive.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad RoomQuestion que vincula salas con preguntas seleccionadas
 * Mantiene el orden y el estado abierto/cerrado de cada pregunta en el juego
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
    private Integer orderNum; // Orden en el que aparece esta pregunta (1, 2, 3...)

    @Column(name = "is_open", nullable = false)
    private Boolean isOpen = false; // Si los jugadores pueden responder esta pregunta ahora

    @Column(name = "start_time")
    private LocalDateTime startTime; // Cuándo se abrió esta pregunta

    @Column(name = "close_time")
    private LocalDateTime closeTime; // Cuándo se cerró esta pregunta

    /**
 * Abrir la pregunta para respuestas
 */
    public void open() {
        this.isOpen = true;
        this.startTime = LocalDateTime.now();
    }

    /**
 * Cerrar la pregunta (tiempo agotado o todos respondieron)
 */
    public void close() {
        this.isOpen = false;
        this.closeTime = LocalDateTime.now();
    }

    /**
 * Comprobar si la pregunta sigue aceptando respuestas
 */
    public boolean canAcceptAnswers() {
        return isOpen && startTime != null && closeTime == null;
    }
}