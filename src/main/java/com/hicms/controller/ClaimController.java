package com.hicms.controller;

import com.hicms.dto.ClaimDTO;
import com.hicms.dto.ClaimReviewDTO;
import com.hicms.entity.*;
import com.hicms.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

/**
 * Controller for Claim Management
 */
@Controller
@RequestMapping("/claims")
@RequiredArgsConstructor
public class ClaimController {
    
    private final ClaimService claimService;
    private final PolicyEnrollmentService enrollmentService;
    private final UserService userService;
    private final DocumentService documentService;
    
    @GetMapping
    public String listClaims(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Claim> claims;
        
        switch (user.getRole()) {
            case ADMIN:
                claims = claimService.findAllClaims();
                break;
            case AGENT:
                claims = claimService.findClaimsByAgent(user.getUserId());
                break;
            case CLAIM_ADJUSTER:
                claims = claimService.findClaimsByAdjuster(user.getUserId());
                break;
            default:
                claims = claimService.findClaimsByClaimant(user.getUserId());
        }
        
        model.addAttribute("claims", claimService.convertToDTOList(claims));
        model.addAttribute("userRole", user.getRole());
        return "claim/list";
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLAIM_ADJUSTER')")
    public String listPendingClaims(Model model) {
        List<Claim> pendingClaims = claimService.findPendingClaims();
        model.addAttribute("claims", claimService.convertToDTOList(pendingClaims));
        model.addAttribute("title", "Pending Claims");
        return "claim/list";
    }
    
    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLAIM_ADJUSTER')")
    public String listUnassignedClaims(Model model) {
        List<Claim> unassignedClaims = claimService.findUnassignedClaims();
        model.addAttribute("claims", claimService.convertToDTOList(unassignedClaims));
        model.addAttribute("title", "Unassigned Claims");
        return "claim/list";
    }
    
    @GetMapping("/view/{id}")
    public String viewClaim(@PathVariable Long id, Model model) {
        Claim claim = claimService.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        model.addAttribute("claim", claimService.convertToDTO(claim));
        model.addAttribute("documents", documentService.convertToDTOList(
                documentService.findByClaimId(id)));
        model.addAttribute("statuses", ClaimStatus.values());
        
        return "claim/view";
    }
    
    @GetMapping("/submit")
    @PreAuthorize("hasAnyRole('USER', 'AGENT')")
    public String submitClaimForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("claim", new ClaimDTO());
        model.addAttribute("userRole", user.getRole());
        
        if (user.getRole() == Role.AGENT) {
            // Agent can submit claims for customers
            model.addAttribute("customers", userService.findUsersByRole(Role.USER));
        } else {
            // User can only submit claims for their active enrollments
            List<PolicyEnrollment> activeEnrollments = 
                    enrollmentService.findActiveEnrollmentsByUser(user.getUserId());
            model.addAttribute("enrollments", enrollmentService.convertToDTOList(activeEnrollments));
        }
        
        return "claim/submit";
    }
    
    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('USER', 'AGENT')")
    public String submitClaim(@Valid @ModelAttribute("claim") ClaimDTO claimDTO,
                              BindingResult result,
                              @RequestParam(required = false) Long customerId,
                              @RequestParam(value = "documents", required = false) List<MultipartFile> documents,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        
        User currentUser = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (result.hasErrors()) {
            model.addAttribute("userRole", currentUser.getRole());
            if (currentUser.getRole() == Role.AGENT) {
                model.addAttribute("customers", userService.findUsersByRole(Role.USER));
            } else {
                List<PolicyEnrollment> activeEnrollments = 
                        enrollmentService.findActiveEnrollmentsByUser(currentUser.getUserId());
                model.addAttribute("enrollments", enrollmentService.convertToDTOList(activeEnrollments));
            }
            return "claim/submit";
        }
        
        try {
            User claimant;
            User agent = null;
            Claim savedClaim;
            
            if (currentUser.getRole() == Role.AGENT && customerId != null) {
                // Agent submitting for customer
                claimant = userService.findById(customerId)
                        .orElseThrow(() -> new RuntimeException("Customer not found"));
                agent = currentUser;
                savedClaim = claimService.submitClaimWithAgent(claimDTO, claimant, agent);
            } else {
                // User submitting for themselves
                claimant = currentUser;
                savedClaim = claimService.submitClaim(claimDTO, currentUser);
            }
            
            // Process uploaded documents
            if (documents != null && !documents.isEmpty()) {
                for (MultipartFile file : documents) {
                    if (!file.isEmpty()) {
                        try {
                            documentService.uploadDocument(savedClaim.getClaimId(), file, currentUser);
                        } catch (Exception e) {
                            // Log but continue - don't fail the whole claim submission
                            System.err.println("Failed to upload document: " + e.getMessage());
                        }
                    }
                }
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Claim submitted successfully!");
            return "redirect:/claims";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to submit claim: " + e.getMessage());
            model.addAttribute("userRole", currentUser.getRole());
            if (currentUser.getRole() == Role.AGENT) {
                model.addAttribute("customers", userService.findUsersByRole(Role.USER));
            } else {
                List<PolicyEnrollment> activeEnrollments = 
                        enrollmentService.findActiveEnrollmentsByUser(currentUser.getUserId());
                model.addAttribute("enrollments", enrollmentService.convertToDTOList(activeEnrollments));
            }
            return "claim/submit";
        }
    }
    
    @PostMapping("/assign/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLAIM_ADJUSTER')")
    public String assignClaim(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User adjuster = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            claimService.assignAdjuster(id, adjuster);
            redirectAttributes.addFlashAttribute("successMessage", "Claim assigned successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to assign claim: " + e.getMessage());
        }
        return "redirect:/claims/view/" + id;
    }
    
    @GetMapping("/review/{id}")
    @PreAuthorize("hasAnyRole('CLAIM_ADJUSTER', 'ADMIN')")
    public String reviewClaimForm(@PathVariable Long id, Model model) {
        Claim claim = claimService.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        model.addAttribute("claim", claimService.convertToDTO(claim));
        model.addAttribute("review", new ClaimReviewDTO());
        model.addAttribute("statuses", new ClaimStatus[]{ClaimStatus.APPROVED, ClaimStatus.REJECTED});
        model.addAttribute("documents", documentService.convertToDTOList(
                documentService.findByClaimId(id)));
        
        return "claim/review";
    }
    
    @PostMapping("/review/{id}")
    @PreAuthorize("hasAnyRole('CLAIM_ADJUSTER', 'ADMIN')")
    public String reviewClaim(@PathVariable Long id,
                               @ModelAttribute("review") ClaimReviewDTO reviewDTO,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User adjuster = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            claimService.reviewClaim(id, reviewDTO, adjuster);
            redirectAttributes.addFlashAttribute("successMessage", "Claim reviewed successfully!");
            return "redirect:/claims";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to review claim: " + e.getMessage());
            return "redirect:/claims/review/" + id;
        }
    }
    
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('USER')")
    public String editClaimForm(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Claim claim = claimService.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        // Verify user owns this claim
        if (!claim.getClaimant().getUserId().equals(user.getUserId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own claims");
            return "redirect:/claims";
        }
        
        // Verify claim is still editable
        if (claim.getClaimStatus() != ClaimStatus.PENDING) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only pending claims can be edited");
            return "redirect:/claims/view/" + id;
        }
        
        model.addAttribute("claim", claimService.convertToDTO(claim));
        return "claim/edit";
    }
    
    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('USER')")
    public String editClaim(@PathVariable Long id,
                            @Valid @ModelAttribute("claim") ClaimDTO claimDTO,
                            BindingResult result,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Claim claim = claimService.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        // Verify user owns this claim
        if (!claim.getClaimant().getUserId().equals(user.getUserId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own claims");
            return "redirect:/claims";
        }
        
        // Verify claim is still editable
        if (claim.getClaimStatus() != ClaimStatus.PENDING) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only pending claims can be edited");
            return "redirect:/claims/view/" + id;
        }
        
        if (result.hasErrors()) {
            return "claim/edit";
        }
        
        try {
            claimService.updateClaim(id, claimDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Claim updated successfully!");
            return "redirect:/claims/view/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to update claim: " + e.getMessage());
            return "claim/edit";
        }
    }
    
    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasRole('USER')")
    public String cancelClaim(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Claim claim = claimService.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        // Verify user owns this claim
        if (!claim.getClaimant().getUserId().equals(user.getUserId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only cancel your own claims");
            return "redirect:/claims";
        }
        
        // Verify claim can be cancelled
        if (claim.getClaimStatus() != ClaimStatus.PENDING) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only pending claims can be cancelled");
            return "redirect:/claims/view/" + id;
        }
        
        try {
            claimService.updateClaimStatus(id, ClaimStatus.CANCELLED);
            redirectAttributes.addFlashAttribute("successMessage", "Claim cancelled successfully!");
            return "redirect:/claims";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel claim: " + e.getMessage());
            return "redirect:/claims/view/" + id;
        }
    }
}
