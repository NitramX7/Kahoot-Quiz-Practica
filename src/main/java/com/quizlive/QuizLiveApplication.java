package com.quizlive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Quiz Live Application - Multi-room concurrent quiz system
 * 
 * This application implements a Quizizz-like platform with:
 * - Spring Boot MVC architecture
 * - Multi-user block and question management
 * - Multi-room concurrent game engine
 * - Thread-safe answer processing
 * - Real-time timers per question
 * 
 * @author Quiz Live Team
 * @version 1.0.0
 */
@SpringBootApplication
public class QuizLiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizLiveApplication.class, args);
        System.out.println("\n" +
                "=================================================\n" +
                "   Quiz Live Application Started Successfully   \n" +
                "=================================================\n" +
                "   Access at: http://localhost:8080             \n" +
                "   H2 Console: http://localhost:8080/h2-console \n" +
                "=================================================\n");
    }
}
