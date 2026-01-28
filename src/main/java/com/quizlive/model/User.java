package com.quizlive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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
 * Entidad User que representa a los anfitriones de quizzes
 * Cada usuario puede crear y gestionar sus propios bloques y salas
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    @Email(message = "Invalid email format")
    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String role = "HOST"; // Rol HOST por defecto

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Campos de perfil
    @Column(name = "avatar_color", length = 7)
    private String avatarColor = "#2563EB"; // Color azul por defecto

    @Column(name = "display_name", length = 100)
    private String displayName; // Nombre visible opcional

    @Column(name = "total_points")
    private Integer totalPoints = 0;

    @Column(name = "quizzes_won")
    private Integer quizzesWon = 0;

    @Column(name = "bio", length = 500)
    private String bio; // Biografía del usuario

    @Column(nullable = false)
    private boolean enabled = true;

    // Relaciones
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Block> blocks = new ArrayList<>();

    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();

    // Métodos auxiliares
    public void addBlock(Block block) {
        blocks.add(block);
        block.setOwner(this);
    }

    public void removeBlock(Block block) {
        blocks.remove(block);
        block.setOwner(null);
    }
}