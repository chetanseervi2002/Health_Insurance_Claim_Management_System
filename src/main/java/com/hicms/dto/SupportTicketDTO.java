package com.hicms.dto;

import com.hicms.entity.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

/**
 * DTO for support ticket
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketDTO {
    
    private Long ticketId;
    
    private String ticketNumber;
    
    private Long userId;
    
    private String userName;
    
    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must not exceed 200 characters")
    private String subject;
    
    @NotBlank(message = "Issue description is required")
    private String issueDescription;
    
    private TicketStatus ticketStatus;
    
    private String priority;
    
    private String resolution;
    
    private Long assignedToId;
    
    private String assignedToName;
    
    private LocalDate createdDate;
    
    private String resolvedDate;
}
