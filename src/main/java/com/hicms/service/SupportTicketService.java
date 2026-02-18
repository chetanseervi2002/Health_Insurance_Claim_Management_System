package com.hicms.service;

import com.hicms.dto.SupportTicketDTO;
import com.hicms.entity.SupportTicket;
import com.hicms.entity.TicketStatus;
import com.hicms.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for SupportTicket operations
 */
public interface SupportTicketService {
    
    SupportTicket createTicket(SupportTicketDTO ticketDTO, User user);
    
    Optional<SupportTicket> findById(Long ticketId);
    
    Optional<SupportTicket> findByTicketNumber(String ticketNumber);
    
    List<SupportTicket> findAllTickets();
    
    List<SupportTicket> findTicketsByUser(Long userId);
    
    List<SupportTicket> findTicketsByStatus(TicketStatus status);
    
    List<SupportTicket> findOpenTickets();
    
    List<SupportTicket> findTicketsAssignedTo(Long userId);
    
    SupportTicket assignTicket(Long ticketId, User assignee);
    
    SupportTicket updateTicketStatus(Long ticketId, TicketStatus status);
    
    SupportTicket resolveTicket(Long ticketId, String resolution);
    
    SupportTicketDTO convertToDTO(SupportTicket ticket);
    
    List<SupportTicketDTO> convertToDTOList(List<SupportTicket> tickets);
    
    String generateTicketNumber();
    
    long countTicketsByStatus(TicketStatus status);
}
