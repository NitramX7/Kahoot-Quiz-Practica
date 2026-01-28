package com.quizlive.controller;

import com.quizlive.model.User;
import com.quizlive.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de inicio
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;

    @GetMapping("/")
    public String home(java.security.Principal principal, org.springframework.ui.Model model) {
        if (principal != null) {
            try {
                User user = userService.findByUsername(principal.getName());
                model.addAttribute("user", user);
            } catch (Exception e) {
                // Ignorar si el usuario no se encuentra por algún motivo
            }
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}