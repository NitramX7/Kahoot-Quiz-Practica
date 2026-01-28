package com.quizlive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Block que representa una colección de preguntas (banco de preguntas)
 * Cada bloque pertenece a un usuario y puede usarse en salas según las preguntas disponibles
 */
@Entity
@Table(name = "blocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Block name is required")
    @Size(min = 3, max = 100, message = "Block name must be between 3 and 100 characters")
    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relaciones
    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "block")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Room> rooms = new ArrayList<>();

    // Métodos auxiliares
    public void addQuestion(Question question) {
        questions.add(question);
        question.setBlock(this);
    }

    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setBlock(null);
    }

    /**
 * Comprobar si este bloque puede eliminarse (ninguna sala lo usa)
 */
    public boolean canBeDeleted() {
        return rooms.isEmpty();
    }
}