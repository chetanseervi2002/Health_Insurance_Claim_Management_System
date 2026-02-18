package com.hicms.controller;

import com.hicms.dto.PolicyDTO;
import com.hicms.entity.Policy;
import com.hicms.entity.PolicyStatus;
import com.hicms.entity.User;
import com.hicms.service.PolicyService;
import com.hicms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Policy Management
 */
@Controller
@RequestMapping("/policies")
@RequiredArgsConstructor
public class PolicyController {
    
    private final PolicyService policyService;
    private final UserService userService;
    
    @GetMapping
    public String listPolicies(Model model) {
        List<Policy> policies = policyService.findAllPolicies();
        List<PolicyDTO> policyDTOs = policies.stream()
                .map(policyService::convertToDTO)
                .collect(Collectors.toList());
        model.addAttribute("policies", policyDTOs);
        return "policy/list";
    }
    
    @GetMapping("/active")
    public String listActivePolicies(Model model) {
        List<Policy> policies = policyService.findActivePolicies();
        List<PolicyDTO> policyDTOs = policies.stream()
                .map(policyService::convertToDTO)
                .collect(Collectors.toList());
        model.addAttribute("policies", policyDTOs);
        return "policy/list";
    }
    
    @GetMapping("/view/{id}")
    public String viewPolicy(@PathVariable Long id, Model model) {
        Policy policy = policyService.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        model.addAttribute("policy", policyService.convertToDTO(policy));
        return "policy/view";
    }
    
    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createPolicyForm(Model model) {
        model.addAttribute("policy", new PolicyDTO());
        model.addAttribute("statuses", PolicyStatus.values());
        return "policy/create";
    }
    
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createPolicy(@Valid @ModelAttribute("policy") PolicyDTO policyDTO,
                               BindingResult result,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("statuses", PolicyStatus.values());
            return "policy/create";
        }
        
        try {
            User admin = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            policyService.createPolicy(policyDTO, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Policy created successfully!");
            return "redirect:/policies";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create policy: " + e.getMessage());
            model.addAttribute("statuses", PolicyStatus.values());
            return "policy/create";
        }
    }
    
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editPolicyForm(@PathVariable Long id, Model model) {
        Policy policy = policyService.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        model.addAttribute("policy", policyService.convertToDTO(policy));
        model.addAttribute("statuses", PolicyStatus.values());
        return "policy/edit";
    }
    
    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updatePolicy(@PathVariable Long id,
                               @Valid @ModelAttribute("policy") PolicyDTO policyDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("statuses", PolicyStatus.values());
            return "policy/edit";
        }
        
        try {
            policyService.updatePolicy(id, policyDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Policy updated successfully!");
            return "redirect:/policies";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to update policy: " + e.getMessage());
            model.addAttribute("statuses", PolicyStatus.values());
            return "policy/edit";
        }
    }
    
    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String cancelPolicy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            policyService.updatePolicyStatus(id, PolicyStatus.CANCELLED);
            redirectAttributes.addFlashAttribute("successMessage", "Policy cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel policy: " + e.getMessage());
        }
        return "redirect:/policies";
    }
    
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deletePolicy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Policy policy = policyService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Policy not found"));
            
            // Only allow deletion of cancelled or inactive policies
            if (policy.getPolicyStatus() == PolicyStatus.ACTIVE) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete an active policy. Please cancel it first.");
                return "redirect:/policies";
            }
            
            policyService.deletePolicy(id);
            redirectAttributes.addFlashAttribute("successMessage", "Policy deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete policy: " + e.getMessage());
        }
        return "redirect:/policies";
    }
    
    @PostMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updatePolicyStatus(@PathVariable Long id,
                                     @RequestParam PolicyStatus status,
                                     RedirectAttributes redirectAttributes) {
        try {
            policyService.updatePolicyStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Policy status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update policy status: " + e.getMessage());
        }
        return "redirect:/policies";
    }
}
