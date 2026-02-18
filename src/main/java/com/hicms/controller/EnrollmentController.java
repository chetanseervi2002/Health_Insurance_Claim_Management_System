package com.hicms.controller;

import com.hicms.entity.*;
import com.hicms.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

/**
 * Controller for Policy Enrollment Management
 */
@Controller
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    
    private final PolicyEnrollmentService enrollmentService;
    private final PolicyService policyService;
    private final UserService userService;
    
    @GetMapping
    public String listEnrollments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<PolicyEnrollment> enrollments;
        
        switch (user.getRole()) {
            case ADMIN:
            case CLAIM_ADJUSTER:
                enrollments = enrollmentService.findAllEnrollments();
                break;
            case AGENT:
                enrollments = enrollmentService.findByAgent(user.getUserId());
                break;
            default:
                enrollments = enrollmentService.findByPolicyholder(user.getUserId());
        }
        
        model.addAttribute("enrollments", enrollmentService.convertToDTOList(enrollments));
        model.addAttribute("userRole", user.getRole());
        return "enrollment/list";
    }
    
    @GetMapping("/view/{id}")
    public String viewEnrollment(@PathVariable Long id, Model model) {
        PolicyEnrollment enrollment = enrollmentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        model.addAttribute("enrollment", enrollmentService.convertToDTO(enrollment));
        return "enrollment/view";
    }
    
    @GetMapping("/enroll")
    @PreAuthorize("hasAnyRole('USER', 'AGENT')")
    public String enrollForm(@RequestParam(required = false) Long policyId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Policy> activePolicies = policyService.findActivePolicies();
        model.addAttribute("policies", activePolicies.stream()
                .map(policyService::convertToDTO)
                .toList());
        model.addAttribute("selectedPolicyId", policyId);
        model.addAttribute("userRole", user.getRole());
        
        // If agent, show customer list
        if (user.getRole() == Role.AGENT) {
            model.addAttribute("customers", userService.findUsersByRole(Role.USER));
        }
        
        return "enrollment/enroll";
    }
    
    @PostMapping("/enroll")
    @PreAuthorize("hasAnyRole('USER', 'AGENT')")
    public String enrollInPolicy(@RequestParam Long policyId,
                                  @RequestParam(required = false) Long customerId,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            User policyholder;
            User agent = null;
            
            if (currentUser.getRole() == Role.AGENT && customerId != null) {
                // Agent enrolling a customer
                policyholder = userService.findById(customerId)
                        .orElseThrow(() -> new RuntimeException("Customer not found"));
                agent = currentUser;
            } else {
                // User enrolling themselves
                policyholder = currentUser;
            }
            
            enrollmentService.enrollInPolicy(policyId, policyholder, agent);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully enrolled in policy!");
            return "redirect:/enrollments";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to enroll: " + e.getMessage());
            return "redirect:/enrollments/enroll?policyId=" + policyId;
        }
    }
    
    @PostMapping("/cancel/{id}")
    public String cancelEnrollment(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            PolicyEnrollment enrollment = enrollmentService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Enrollment not found"));
            
            // Check authorization
            boolean canCancel = currentUser.getRole() == Role.ADMIN ||
                    (currentUser.getRole() == Role.USER && 
                     enrollment.getPolicyholder().getUserId().equals(currentUser.getUserId())) ||
                    (currentUser.getRole() == Role.AGENT && 
                     enrollment.getAgent() != null && 
                     enrollment.getAgent().getUserId().equals(currentUser.getUserId()));
            
            if (!canCancel) {
                redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to cancel this enrollment");
                return "redirect:/enrollments";
            }
            
            enrollmentService.cancelEnrollment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Enrollment cancelled successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel enrollment: " + e.getMessage());
        }
        return "redirect:/enrollments";
    }
    
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteEnrollment(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            PolicyEnrollment enrollment = enrollmentService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Enrollment not found"));
            
            // Only allow deletion of cancelled enrollments
            if (enrollment.getEnrollmentStatus() != PolicyEnrollment.EnrollmentStatus.CANCELLED) {
                redirectAttributes.addFlashAttribute("errorMessage", "Only cancelled enrollments can be deleted");
                return "redirect:/enrollments";
            }
            
            enrollmentService.deleteEnrollment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Enrollment deleted successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete enrollment: " + e.getMessage());
        }
        return "redirect:/enrollments";
    }



    
}
