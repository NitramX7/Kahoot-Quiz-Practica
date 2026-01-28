package com.quizlive.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Room que representa una sesión de juego con PIN único
 * Soporta estados: WAITING, RUNNING, FINISHED
 */
@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 6)
    private String pin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "block_id", nullable = false)
    private Block block;

    @Min(value = 1, message = "Number of questions must be at least 1")
    @Column(name = "num_questions", nullable = false)
    private Integer numQuestions;

    @Enumerated(EnumType.STRING)
    @Column(name = "selection_mode", nullable = false, length = 20)
    private SelectionMode selectionMode = SelectionMode.MANUAL;

    @Min(value = 5, message = "Time per question must be at least 5 seconds")
    @Column(name = "time_per_question", nullable = false)
    private Integer timePerQuestion; // en segundos

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomState state = RoomState.WAITING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    // Relaciones
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomQuestion> roomQuestions = new ArrayList<>();

    // Métodos auxiliares
    public void addPlayer(Player player) {
        players.add(player);
        player.setRoom(this);
    }

    public void removePlayer(Player player) {
        players.remove(player);
        player.setRoom(null);
    }

    public void addRoomQuestion(RoomQuestion roomQuestion) {
        roomQuestions.add(roomQuestion);
        roomQuestion.setRoom(this);
    }

    public boolean isWaiting() {
        return state == RoomState.WAITING;
    }

    public boolean isRunning() {
        return state == RoomState.RUNNING;
    }

    public boolean isFinished() {
        return state == RoomState.FINISHED;
    }

    public void start() {
        if (!isWaiting()) {
            throw new IllegalStateException("Room can only be started from WAITING state");
        }
        this.state = RoomState.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public void finish() {
        if (!isRunning()) {
            throw new IllegalStateException("Room can only be finished from RUNNING state");
        }
        this.state = RoomState.FINISHED;
        this.finishedAt = LocalDateTime.now();
    }

    // Enumeraciones
    public enum RoomState {
        WAITING,
        RUNNING,
        FINISHED
    }

    public enum SelectionMode {
        MANUAL,
        RANDOM
    }
}