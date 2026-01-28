package com.quizlive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación Quiz Live - Sistema de cuestionarios concurrentes multi-sala
 *
 * Esta aplicación implementa una plataforma tipo Quizizz con:
 * - Arquitectura Spring Boot MVC
 * - Gestión de bloques y preguntas multiusuario
 * - Motor de juego concurrente multi-sala
 * - Procesamiento de respuestas thread-safe
 * - Temporizadores en tiempo real por pregunta
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