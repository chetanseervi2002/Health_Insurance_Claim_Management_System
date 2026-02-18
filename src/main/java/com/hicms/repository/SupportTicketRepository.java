package com.hicms.repository;

import com.hicms.entity.SupportTicket;
import com.hicms.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SupportTicket entity
 */
@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    
    Optional<SupportTicket> findByTicketNumber(String ticketNumber);
    
    List<SupportTicket> findByUserUserId(Long userId);
    
    List<SupportTicket> findByTicketStatus(TicketStatus status);
    
    List<SupportTicket> findByAssignedToUserId(Long userId);
    
    @Query("SELECT st FROM SupportTicket st WHERE st.ticketStatus = 'OPEN' OR st.ticketStatus = 'IN_PROGRESS'")
    List<SupportTicket> findOpenTickets();
    
    @Query("SELECT COUNT(st) FROM SupportTicket st WHERE st.ticketStatus = :status")
    long countByTicketStatus(@Param("status") TicketStatus status);
    
    List<SupportTicket> findByUserUserIdOrderByCreatedDateDesc(Long userId);
}
