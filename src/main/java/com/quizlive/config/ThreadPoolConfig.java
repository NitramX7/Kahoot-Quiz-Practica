package com.quizlive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Configuración del pool de hilos para requisitos de concurrencia PSP
 *
 * Esta configuración crea:
 * - ExecutorService: pool de hilos para procesar respuestas de jugadores de forma concurrente
 * - ScheduledExecutorService: pool de temporizadores para tiempos de espera de preguntas
 *
 * Requisitos PSP:
 * - Varias salas pueden procesar respuestas simultáneamente
 * - Cada sala tiene temporizadores independientes
 * - Operaciones concurrentes thread-safe
 */
@Configuration
public class ThreadPoolConfig {

    /**
 * Pool de hilos para procesar respuestas de jugadores
 * El tamaño fijo del pool permite un procesamiento concurrente controlado
 */
    @Bean(name = "answerProcessingExecutor")
    public ExecutorService answerProcessingExecutor() {
        int poolSize = 10; // Se puede configurar vía application.properties
        return Executors.newFixedThreadPool(poolSize, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("answer-pool-" + thread.getId());
            return thread;
        });
    }

    /**
 * Ejecutor programado para temporizadores de preguntas
 * Permite que varias salas tengan temporizadores de cuenta atrás independientes
 */
    @Bean(name = "timerExecutor")
    public ScheduledExecutorService timerExecutor() {
        int poolSize = 10; // Soporta 10+ temporizadores de salas concurrentes
        return Executors.newScheduledThreadPool(poolSize, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("timer-pool-" + thread.getId());
            return thread;
        });
    }
}