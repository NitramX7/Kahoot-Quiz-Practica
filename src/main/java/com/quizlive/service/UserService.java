package com.quizlive.service;

import com.quizlive.model.User;
import com.quizlive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for User management and authentication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new user
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
     * Find user by username
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    /**
     * Update username (checking for uniqueness)
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
     * Save/Update user
     */
    @Transactional
    public User save(User user) {
        log.info("Updating user: {}", user.getUsername());
        return userRepository.save(user);
    }
}
