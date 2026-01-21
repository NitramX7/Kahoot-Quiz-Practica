package com.quizlive.controller;

import com.quizlive.model.Block;
import com.quizlive.model.User;
import com.quizlive.service.BlockService;
import com.quizlive.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Block (question collection) management
 */
@Controller
@RequestMapping("/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;
    private final UserService userService;

    /**
     * List all blocks for current user
     */
    @GetMapping
    public String listBlocks(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Block> blocks = blockService.getBlocksByUser(user.getId());
        
        model.addAttribute("blocks", blocks);
        model.addAttribute("user", user);
        return "blocks/list";
    }

    /**
     * Show create block form
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        return "blocks/create";
    }

    /**
     * Create new block
     */
    @PostMapping
    public String createBlock(@RequestParam String name,
                             @RequestParam(required = false) String description,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        blockService.createBlock(name, description, user);
        return "redirect:/blocks";
    }

    /**
     * Show edit block form
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        Block block = blockService.getBlockById(id, user.getId());
        model.addAttribute("block", block);
        return "blocks/edit";
    }

    /**
     * Update block
     */
    @PostMapping("/{id}")
    public String updateBlock(@PathVariable Long id,
                             @RequestParam String name,
                             @RequestParam(required = false) String description,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        blockService.updateBlock(id, name, description, user.getId());
        return "redirect:/blocks";
    }

    /**
     * Delete block
     */
    @PostMapping("/{id}/delete")
    public String deleteBlock(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        try {
            blockService.deleteBlock(id, user.getId());
        } catch (IllegalStateException e) {
            // Block is being used by rooms
            return "redirect:/blocks?error=" + e.getMessage();
        }
        return "redirect:/blocks";
    }

    /**
     * View block details with questions
     */
    @GetMapping("/{id}")
    public String viewBlock(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        Block block = blockService.getBlockById(id, user.getId());
        model.addAttribute("block", block);
        return "blocks/view";
    }
}
