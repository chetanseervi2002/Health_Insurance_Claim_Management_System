package com.hicms.controller;

import com.hicms.entity.*;
import com.hicms.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

/**
 * Dashboard Controller - shows role-specific dashboards
 */
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final UserService userService;
    private final PolicyService policyService;
    private final PolicyEnrollmentService enrollmentService;
    private final ClaimService claimService;
    private final SupportTicketService ticketService;
    
    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", userService.convertToDTO(user));
        
        switch (user.getRole()) {
            case ADMIN:
                return adminDashboard(model);
            case AGENT:
                return agentDashboard(user, model);
            case CLAIM_ADJUSTER:
                return adjusterDashboard(user, model);
            case USER:
            default:
                return userDashboard(user, model);
        }
    }
    
    private String adminDashboard(Model model) {
        // Policy statistics
        List<Policy> allPolicies = policyService.findAllPolicies();
        model.addAttribute("totalPolicies", allPolicies.size());
        model.addAttribute("activePolicies", policyService.findActivePolicies().size());
        
        // Claim statistics
        model.addAttribute("totalClaims", claimService.findAllClaims().size());
        model.addAttribute("pendingClaims", claimService.countClaimsByStatus(ClaimStatus.PENDING));
        model.addAttribute("approvedClaims", claimService.countClaimsByStatus(ClaimStatus.APPROVED));
        model.addAttribute("rejectedClaims", claimService.countClaimsByStatus(ClaimStatus.REJECTED));
        
        // User statistics
        model.addAttribute("totalUsers", userService.findAllUsers().size());
        model.addAttribute("totalAgents", userService.findUsersByRole(Role.AGENT).size());
        model.addAttribute("totalAdjusters", userService.findUsersByRole(Role.CLAIM_ADJUSTER).size());
        
        // Support ticket statistics
        model.addAttribute("openTickets", ticketService.countTicketsByStatus(TicketStatus.OPEN));
        
        // Recent data
        model.addAttribute("recentClaims", claimService.findPendingClaims());
        model.addAttribute("recentTickets", ticketService.findOpenTickets());
        
        return "dashboard/admin";
    }
    
    private String agentDashboard(User agent, Model model) {
        // Agent's enrollments
        List<PolicyEnrollment> myEnrollments = enrollmentService.findByAgent(agent.getUserId());
        model.addAttribute("myEnrollments", enrollmentService.convertToDTOList(myEnrollments));
        model.addAttribute("totalEnrollments", myEnrollments.size());
        
        // Agent's claims
        List<Claim> myClaims = claimService.findClaimsByAgent(agent.getUserId());
        model.addAttribute("myClaims", claimService.convertToDTOList(myClaims));
        model.addAttribute("totalClaims", myClaims.size());
        
        // Available policies
        model.addAttribute("activePolicies", policyService.findActivePolicies().size());
        
        // All customers
        model.addAttribute("totalCustomers", userService.findUsersByRole(Role.USER).size());
        
        // Support tickets assigned to agent
        List<SupportTicket> assignedTickets = ticketService.findTicketsAssignedTo(agent.getUserId());
        model.addAttribute("assignedTickets", ticketService.convertToDTOList(assignedTickets));
        model.addAttribute("totalAssignedTickets", assignedTickets.size());
        
        // Open tickets
        List<SupportTicket> openTickets = ticketService.findOpenTickets();
        model.addAttribute("openTickets", ticketService.convertToDTOList(openTickets));
        model.addAttribute("totalOpenTickets", openTickets.size());
        
        return "dashboard/agent";
    }
    
    private String adjusterDashboard(User adjuster, Model model) {
        // Claims assigned to this adjuster
        List<Claim> assignedClaims = claimService.findClaimsByAdjuster(adjuster.getUserId());
        model.addAttribute("assignedClaims", claimService.convertToDTOList(assignedClaims));
        model.addAttribute("totalAssigned", assignedClaims.size());
        
        // Pending claims for review
        List<Claim> pendingClaims = claimService.findPendingClaims();
        model.addAttribute("pendingClaims", claimService.convertToDTOList(pendingClaims));
        model.addAttribute("totalPending", pendingClaims.size());
        
        // Unassigned claims
        List<Claim> unassignedClaims = claimService.findUnassignedClaims();
        model.addAttribute("unassignedClaims", claimService.convertToDTOList(unassignedClaims));
        model.addAttribute("totalUnassigned", unassignedClaims.size());
        
        // Statistics
        model.addAttribute("approvedCount", claimService.countClaimsByStatus(ClaimStatus.APPROVED));
        model.addAttribute("rejectedCount", claimService.countClaimsByStatus(ClaimStatus.REJECTED));
        
        return "dashboard/adjuster";
    }
    
    private String userDashboard(User user, Model model) {
        // User's enrollments
        List<PolicyEnrollment> myEnrollments = enrollmentService.findByPolicyholder(user.getUserId());
        model.addAttribute("myEnrollments", enrollmentService.convertToDTOList(myEnrollments));
        model.addAttribute("totalEnrollments", myEnrollments.size());
        
        // Active enrollments
        List<PolicyEnrollment> activeEnrollments = enrollmentService.findActiveEnrollmentsByUser(user.getUserId());
        model.addAttribute("activeEnrollments", activeEnrollments.size());
        
        // User's claims
        List<Claim> myClaims = claimService.findClaimsByClaimant(user.getUserId());
        model.addAttribute("myClaims", claimService.convertToDTOList(myClaims));
        model.addAttribute("totalClaims", myClaims.size());
        
        // User's support tickets
        List<SupportTicket> myTickets = ticketService.findTicketsByUser(user.getUserId());
        model.addAttribute("myTickets", ticketService.convertToDTOList(myTickets));
        model.addAttribute("totalTickets", myTickets.size());
        
        // Available policies
        model.addAttribute("availablePolicies", policyService.findActivePolicies().size());
        
        return "dashboard/user";
    }
}
