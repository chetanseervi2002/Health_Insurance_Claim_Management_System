package com.hicms.dto;

import com.hicms.entity.PolicyEnrollment.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

/**
 * DTO for policy enrollment
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyEnrollmentDTO {
    
    private Long enrollmentId;
    
    @NotNull(message = "Policy ID is required")
    private Long policyId;
    
    private String policyName;
    
    private String policyNumber;
    
    private Long policyholderId;
    
    private String policyholderName;
    
    private Long agentId;
    
    private String agentName;
    
    private LocalDate enrollmentDate;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private EnrollmentStatus enrollmentStatus;
    
    private String coverageAmount;
    
    private String premiumAmount;
}
