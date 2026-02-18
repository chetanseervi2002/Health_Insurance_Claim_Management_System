package com.hicms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Policy entity representing insurance policies
 */
@Entity
@Table(name = "policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policyId;
    
    @Column(unique = true, nullable = false, length = 50)
    private String policyNumber;
    
    @Column(nullable = false, length = 100)
    private String policyName;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "coverage_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal coverageAmount;
    
    @Column(name = "premium_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal premiumAmount;
    
    @Column(name = "duration_months")
    private Integer durationMonths;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "policy_status", nullable = false)
    private PolicyStatus policyStatus;
    
    @Column(name = "created_date")
    private LocalDate createdDate;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    // Relationships
    @Builder.Default
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PolicyEnrollment> enrollments = new ArrayList<>();
    
    @Builder.Default
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Claim> claims = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
        updatedDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
