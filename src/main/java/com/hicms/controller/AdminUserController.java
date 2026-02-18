package com.hicms.controller;

import com.hicms.dto.UserDTO;
import com.hicms.dto.UserRegistrationDTO;
import com.hicms.entity.Role;
import com.hicms.entity.User;
import com.hicms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

/**
 * Controller for Admin User Management
 */
@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {
    
    private final UserService userService;
    
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.findAllUsers();
        List<UserDTO> userDTOs = users.stream()
                .map(userService::convertToDTO)
                .toList();
        model.addAttribute("users", userDTOs);
        
        // Add count statistics
        long totalUsers = users.size();
        long customerCount = users.stream().filter(u -> u.getRole() == Role.USER).count();
        long agentCount = users.stream().filter(u -> u.getRole() == Role.AGENT).count();
        long adjusterCount = users.stream().filter(u -> u.getRole() == Role.CLAIM_ADJUSTER).count();
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("customerCount", customerCount);
        model.addAttribute("agentCount", agentCount);
        model.addAttribute("adjusterCount", adjusterCount);
        
        return "admin/users/list";
    }
    
    @GetMapping("/create")
    public String createUserForm(Model model) {
        model.addAttribute("userDTO", new UserRegistrationDTO());
        model.addAttribute("roles", Role.values());
        return "admin/users/create";
    }
    
    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("userDTO") UserRegistrationDTO registrationDTO,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "admin/users/create";
        }
        
        // Validate password confirmation
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Passwords do not match");
            model.addAttribute("roles", Role.values());
            return "admin/users/create";
        }
        
        // Check if username exists
        if (userService.existsByUsername(registrationDTO.getUsername())) {
            result.rejectValue("username", "error.user", "Username already exists");
            model.addAttribute("roles", Role.values());
            return "admin/users/create";
        }
        
        // Check if email exists
        if (userService.existsByEmail(registrationDTO.getEmail())) {
            result.rejectValue("email", "error.user", "Email already exists");
            model.addAttribute("roles", Role.values());
            return "admin/users/create";
        }
        
        try {
            Role role = registrationDTO.getRole() != null ? registrationDTO.getRole() : Role.USER;
            userService.registerUser(registrationDTO, role);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create user: " + e.getMessage());
            model.addAttribute("roles", Role.values());
            return "admin/users/create";
        }
    }
    
    @GetMapping("/view/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", userService.convertToDTO(user));
        return "admin/users/view";
    }
    
    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", userService.convertToDTO(user));
        model.addAttribute("roles", Role.values());
        return "admin/users/edit";
    }
    
    @PostMapping("/edit/{id}")
    public String editUser(@PathVariable Long id, @ModelAttribute("user") UserDTO userDTO,
                           RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, userDTO);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update user: " + e.getMessage());
            return "redirect:/admin/users/edit/" + id;
        }
    }
    
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/enable/{id}")
    public String enableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.enableUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User enabled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to enable user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/disable/{id}")
    public String disableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.disableUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User disabled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to disable user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
