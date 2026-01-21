package com.quizlive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Thread Pool Configuration for PSP Concurrency Requirements
 * 
 * This configuration creates:
 * - ExecutorService: Thread pool for processing player answers concurrently
 * - ScheduledExecutorService: Timer pool for question timeouts
 * 
 * PSP Requirements:
 * - Multiple rooms can process answers simultaneously
 * - Each room has independent timers
 * - Thread-safe concurrent operations
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * Thread pool for processing player answers
     * Fixed pool size allows controlled concurrent answer processing
     */
    @Bean(name = "answerProcessingExecutor")
    public ExecutorService answerProcessingExecutor() {
        int poolSize = 10; // Can be configured via application.properties
        return Executors.newFixedThreadPool(poolSize, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("answer-pool-" + thread.getId());
            return thread;
        });
    }

    /**
     * Scheduled executor for question timers
     * Allows multiple rooms to have independent countdown timers
     */
    @Bean(name = "timerExecutor")
    public ScheduledExecutorService timerExecutor() {
        int poolSize = 10; // Supports 10+ concurrent room timers
        return Executors.newScheduledThreadPool(poolSize, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("timer-pool-" + thread.getId());
            return thread;
        });
    }
}
