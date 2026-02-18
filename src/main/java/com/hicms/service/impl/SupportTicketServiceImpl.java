package com.hicms.service.impl;

import com.hicms.dto.SupportTicketDTO;
import com.hicms.entity.SupportTicket;
import com.hicms.entity.TicketStatus;
import com.hicms.entity.User;
import com.hicms.repository.SupportTicketRepository;
import com.hicms.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of SupportTicketService
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SupportTicketServiceImpl implements SupportTicketService {
    
    private final SupportTicketRepository ticketRepository;
    
    @Override
    public SupportTicket createTicket(SupportTicketDTO ticketDTO, User user) {
        SupportTicket ticket = SupportTicket.builder()
                .ticketNumber(generateTicketNumber())
                .user(user)
                .subject(ticketDTO.getSubject())
                .issueDescription(ticketDTO.getIssueDescription())
                .ticketStatus(TicketStatus.OPEN)
                .priority(ticketDTO.getPriority() != null ? ticketDTO.getPriority() : "MEDIUM")
                .build();
        
        return ticketRepository.save(ticket);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<SupportTicket> findById(Long ticketId) {
        return ticketRepository.findById(ticketId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<SupportTicket> findByTicketNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SupportTicket> findAllTickets() {
        return ticketRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SupportTicket> findTicketsByUser(Long userId) {
        return ticketRepository.findByUserUserIdOrderByCreatedDateDesc(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SupportTicket> findTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByTicketStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SupportTicket> findOpenTickets() {
        return ticketRepository.findOpenTickets();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SupportTicket> findTicketsAssignedTo(Long userId) {
        return ticketRepository.findByAssignedToUserId(userId);
    }
    
    @Override
    public SupportTicket assignTicket(Long ticketId, User assignee) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setAssignedTo(assignee);
        ticket.setTicketStatus(TicketStatus.IN_PROGRESS);
        return ticketRepository.save(ticket);
    }
    
    @Override
    public SupportTicket updateTicketStatus(Long ticketId, TicketStatus status) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setTicketStatus(status);
        
        if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
            ticket.setResolvedDate(LocalDateTime.now());
        }
        
        return ticketRepository.save(ticket);
    }
    
    @Override
    public SupportTicket resolveTicket(Long ticketId, String resolution) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setResolution(resolution);
        ticket.setTicketStatus(TicketStatus.RESOLVED);
        ticket.setResolvedDate(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }
    
    @Override
    public SupportTicketDTO convertToDTO(SupportTicket ticket) {
        return SupportTicketDTO.builder()
                .ticketId(ticket.getTicketId())
                .ticketNumber(ticket.getTicketNumber())
                .userId(ticket.getUser().getUserId())
                .userName(ticket.getUser().getFullName())
                .subject(ticket.getSubject())
                .issueDescription(ticket.getIssueDescription())
                .ticketStatus(ticket.getTicketStatus())
                .priority(ticket.getPriority())
                .resolution(ticket.getResolution())
                .assignedToId(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getUserId() : null)
                .assignedToName(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getFullName() : null)
                .createdDate(ticket.getCreatedDate())
                .resolvedDate(ticket.getResolvedDate() != null ? 
                        ticket.getResolvedDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .build();
    }
    
    @Override
    public List<SupportTicketDTO> convertToDTOList(List<SupportTicket> tickets) {
        return tickets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public String generateTicketNumber() {
        String prefix = "TKT";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + "-" + datePart + "-" + uniquePart;
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countTicketsByStatus(TicketStatus status) {
        return ticketRepository.countByTicketStatus(status);
    }
}
