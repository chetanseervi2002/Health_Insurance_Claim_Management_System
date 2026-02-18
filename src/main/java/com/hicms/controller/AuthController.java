package com.hicms.controller;

import com.hicms.dto.UserRegistrationDTO;
import com.hicms.entity.Role;
import com.hicms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for authentication operations
 */


@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDTO registrationDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "auth/register";
        }
        
        // Validate password confirmation
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Passwords do not match");
            return "auth/register";
        }
        
        // Check if username exists
        if (userService.existsByUsername(registrationDTO.getUsername())) {
            result.rejectValue("username", "error.user", "Username already exists");
            return "auth/register";
        }
        
        // Check if email exists
        if (userService.existsByEmail(registrationDTO.getEmail())) {
            result.rejectValue("email", "error.user", "Email already exists");
            return "auth/register";
        }
        
        try {
            userService.registerUser(registrationDTO, Role.USER);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            result.rejectValue("username", "error.user", "Registration failed: " + e.getMessage());
            return "auth/register";
        }
    }
    
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }

}
