package com.hicms.dto;

import com.hicms.entity.PolicyStatus;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * DTO for policy creation and update
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyDTO {
    
    private Long policyId;
    
    private String policyNumber;
    
    @NotBlank(message = "Policy name is required")
    @Size(max = 100, message = "Policy name must not exceed 100 characters")
    private String policyName;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Coverage amount is required")
    @DecimalMin(value = "0.01", message = "Coverage amount must be greater than 0")
    private BigDecimal coverageAmount;
    
    @NotNull(message = "Premium amount is required")
    @DecimalMin(value = "0.01", message = "Premium amount must be greater than 0")
    private BigDecimal premiumAmount;
    
    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer durationMonths;
    
    private PolicyStatus policyStatus;
    
    private String createdByUsername;
    
    private String createdDate;
}
