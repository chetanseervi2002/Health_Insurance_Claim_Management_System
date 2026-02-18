package com.hicms.controller;

import com.hicms.dto.SupportTicketDTO;
import com.hicms.entity.*;
import com.hicms.service.SupportTicketService;
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

/**
 * Controller for Customer Support Ticket Management
 */
@Controller
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportController {
    
    private final SupportTicketService ticketService;
    private final UserService userService;
    
    @GetMapping
    public String listTickets(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<SupportTicket> tickets;
        
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.AGENT || user.getRole() == Role.CLAIM_ADJUSTER) {
            tickets = ticketService.findAllTickets();
            // Add statistics for agents/admins
            model.addAttribute("openCount", ticketService.countTicketsByStatus(TicketStatus.OPEN));
            model.addAttribute("inProgressCount", ticketService.countTicketsByStatus(TicketStatus.IN_PROGRESS));
            model.addAttribute("resolvedCount", ticketService.countTicketsByStatus(TicketStatus.RESOLVED));
            model.addAttribute("closedCount", ticketService.countTicketsByStatus(TicketStatus.CLOSED));
        } else {
            tickets = ticketService.findTicketsByUser(user.getUserId());
        }
        
        model.addAttribute("tickets", ticketService.convertToDTOList(tickets));
        model.addAttribute("userRole", user.getRole());
        return "support/list";
    }
    
    @GetMapping("/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLAIM_ADJUSTER')")
    public String listOpenTickets(Model model) {
        List<SupportTicket> openTickets = ticketService.findOpenTickets();
        model.addAttribute("tickets", ticketService.convertToDTOList(openTickets));
        model.addAttribute("title", "Open Tickets");
        return "support/list";
    }
    
    @GetMapping("/view/{id}")
    public String viewTicket(@PathVariable Long id, Model model) {
        SupportTicket ticket = ticketService.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        model.addAttribute("ticket", ticketService.convertToDTO(ticket));
        model.addAttribute("statuses", TicketStatus.values());
        
        return "support/view";
    }
    
    @GetMapping("/create")
    public String createTicketForm(Model model) {
        model.addAttribute("ticket", new SupportTicketDTO());
        return "support/create";
    }
    
    @PostMapping("/create")
    public String createTicket(@Valid @ModelAttribute("ticket") SupportTicketDTO ticketDTO,
                                BindingResult result,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        
        if (result.hasErrors()) {
            return "support/create";
        }
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            ticketService.createTicket(ticketDTO, user);
            redirectAttributes.addFlashAttribute("successMessage", "Support ticket created successfully!");
            return "redirect:/support";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create ticket: " + e.getMessage());
            return "support/create";
        }
    }
    
    @PostMapping("/assign/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLAIM_ADJUSTER')")
    public String assignTicket(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            User assignee = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            ticketService.assignTicket(id, assignee);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket assigned successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to assign ticket: " + e.getMessage());
        }
        return "redirect:/support/view/" + id;
    }
    
    @PostMapping("/resolve/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLAIM_ADJUSTER')")
    public String resolveTicket(@PathVariable Long id,
                                 @RequestParam String resolution,
                                 RedirectAttributes redirectAttributes) {
        try {
            ticketService.resolveTicket(id, resolution);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket resolved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to resolve ticket: " + e.getMessage());
        }
        return "redirect:/support";
    }
    
    @PostMapping("/status/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLAIM_ADJUSTER')")
    public String updateTicketStatus(@PathVariable Long id,
                                      @RequestParam TicketStatus status,
                                      RedirectAttributes redirectAttributes) {
        try {
            ticketService.updateTicketStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update ticket status: " + e.getMessage());
        }
        return "redirect:/support/view/" + id;
    }
}
