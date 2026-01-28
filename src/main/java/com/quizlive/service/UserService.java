package com.quizlive.service;

import com.quizlive.model.User;
import com.quizlive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestión de usuarios y autenticación
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
 * Registrar un nuevo usuario
 */
    @Transactional
    public User registerUser(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole("HOST");
        user.setEnabled(true);

        log.info("Registered new user: {}", username);
        return userRepository.save(user);
    }

    /**
 * Buscar usuario por nombre de usuario
 */
    /**
 * Buscar usuario por nombre de usuario
 */
    public User findByUsername(String identifier) {
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + identifier));
    }

    /**
 * Buscar usuario por email
 */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }

    /**
 * Obtener usuario por ID
 */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    /**
 * Actualizar nombre de usuario (comprobando unicidad)
 */
    @Transactional
    public void updateUsername(User user, String newUsername) {
        if (userRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }
        user.setUsername(newUsername);
        userRepository.save(user);
        log.info("Username updated to: {}", newUsername);
    }

    /**
 * Guardar/Actualizar usuario
 */
    @Transactional
    public User save(User user) {
        log.info("Updating user: {}", user.getUsername());
        return userRepository.save(user);
    }
}