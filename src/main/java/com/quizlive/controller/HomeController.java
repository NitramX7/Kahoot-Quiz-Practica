package com.quizlive.controller;

import com.quizlive.model.User;
import com.quizlive.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home controller
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
                // Ignore if user not found for some reason
            }
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
