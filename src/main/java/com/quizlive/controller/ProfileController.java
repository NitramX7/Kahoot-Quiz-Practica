package com.quizlive.controller;

import com.quizlive.model.User;
import com.quizlive.service.BlockService;
import com.quizlive.service.QuestionService;
import com.quizlive.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final BlockService blockService;
    private final QuestionService questionService;
    private final PasswordEncoder passwordEncoder;


    /**
     * Show user profile/settings page
     */
    @GetMapping
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        
        // Calculate statistics
        long blocksCount = blockService.getBlocksByUser(user.getId()).size();
        long questionsCount = questionService.getAllQuestionsByUser(user.getId()).size();
        
        model.addAttribute("user", user);
        model.addAttribute("blocksCount", blocksCount);
        model.addAttribute("questionsCount", questionsCount);
        
        return "profile";
    }

    /**
     * Update user profile information
     */
    @PostMapping("/update")
    public String updateProfile(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String displayName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String avatarColor,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        
        // Update username (check for uniqueness)
        if (username != null && !username.trim().isEmpty() && !username.equals(user.getUsername())) {
            try {
                userService.updateUsername(user, username.trim());
                
                // Update Security Context with new username
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    new org.springframework.security.core.userdetails.User(
                        username.trim(), user.getPassword(), auth.getAuthorities()),
                    auth.getCredentials(),
                    auth.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(newAuth);

            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/profile";
            }
        }
        
        // Update fields
        if (displayName != null && !displayName.trim().isEmpty()) {
            user.setDisplayName(displayName.trim());
        }
        
        if (email != null && !email.trim().isEmpty()) {
            user.setEmail(email.trim());
        }
        
        if (bio != null) {
            user.setBio(bio.trim());
        }
        
        if (avatarColor != null && avatarColor.matches("^#[0-9A-Fa-f]{6}$")) {
            user.setAvatarColor(avatarColor);
        }
        
        userService.save(user);
        
        redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
        return "redirect:/profile";
    }

    /**
     * Change user password
     */
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        
        // Validate current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta");
            return "redirect:/profile";
        }
        
        // Validate new password
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "La nueva contraseña debe tener al menos 6 caracteres");
            return "redirect:/profile";
        }
        
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
            return "redirect:/profile";
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);
        
        redirectAttributes.addFlashAttribute("success", "Contraseña cambiada correctamente");
        return "redirect:/profile";
    }
}
