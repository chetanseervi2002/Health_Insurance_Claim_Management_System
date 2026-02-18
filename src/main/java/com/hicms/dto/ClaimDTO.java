package com.hicms.dto;

import com.hicms.entity.ClaimStatus;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for claim submission and display
**/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimDTO {
    
    private Long claimId;
    
    private String claimNumber;
    
    @NotNull(message = "Policy ID is required")
    private Long policyId;
    
    private String policyName;
    
    private String policyNumber;
    
    private Long claimantId;
    
    private String claimantName;
    
    private Long agentId;
    
    private String agentName;
    
    private Long adjusterId;
    
    private String adjusterName;
    
    @NotNull(message = "Claim amount is required")
    @DecimalMin(value = "0.01", message = "Claim amount must be greater than 0")
    private BigDecimal claimAmount;
    
    private BigDecimal approvedAmount;
    
    private BigDecimal coverageAmount;
    
    private LocalDate claimDate;
    
    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
    
    private ClaimStatus claimStatus;
    
    private String remarks;
    
    private String createdDate;
}
