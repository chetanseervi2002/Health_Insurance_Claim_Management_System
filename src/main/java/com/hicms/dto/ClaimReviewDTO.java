package com.hicms.dto;

import com.hicms.entity.ClaimStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;

/**
 * DTO for claim review by adjuster
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimReviewDTO {
    
    private Long claimId;
    
    private ClaimStatus claimStatus;
    
    @DecimalMin(value = "0.00", message = "Approved amount must be 0 or greater")
    private BigDecimal approvedAmount;
    
    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    private String remarks;
}
