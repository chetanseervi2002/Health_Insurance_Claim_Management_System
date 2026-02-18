package com.hicms.service.impl;

import com.hicms.dto.ClaimDTO;
import com.hicms.dto.ClaimReviewDTO;
import com.hicms.entity.*;
import com.hicms.repository.ClaimRepository;
import com.hicms.repository.PolicyRepository;
import com.hicms.repository.PolicyEnrollmentRepository;
import com.hicms.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ClaimService
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ClaimServiceImpl implements ClaimService {
    
    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final PolicyEnrollmentRepository enrollmentRepository;
    
    @Override
    public Claim submitClaim(ClaimDTO claimDTO, User claimant) {
        return submitClaimWithAgent(claimDTO, claimant, null);
    }
    
    @Override
    public Claim submitClaimWithAgent(ClaimDTO claimDTO, User claimant, User agent) {
        Policy policy = policyRepository.findById(claimDTO.getPolicyId())
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        
        // Verify user is enrolled in the policy
        boolean isEnrolled = enrollmentRepository
                .existsByPolicyholderUserIdAndPolicyPolicyIdAndEnrollmentStatusIn(
                        claimant.getUserId(),
                        claimDTO.getPolicyId(),
                        Arrays.asList(PolicyEnrollment.EnrollmentStatus.ACTIVE)
                );
        
        if (!isEnrolled) {
            throw new RuntimeException("User is not enrolled in this policy");
        }
        
        // Validate claim amount against coverage
        if (claimDTO.getClaimAmount().compareTo(policy.getCoverageAmount()) > 0) {
            throw new RuntimeException("Claim amount exceeds coverage amount");
        }
        
        Claim claim = Claim.builder()
                .claimNumber(generateClaimNumber())
                .policy(policy)
                .claimant(claimant)
                .agent(agent)
                .claimAmount(claimDTO.getClaimAmount())
                .claimDate(LocalDate.now())
                .description(claimDTO.getDescription())
                .reason(claimDTO.getReason())
                .claimStatus(ClaimStatus.PENDING)
                .build();
        
        return claimRepository.save(claim);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Claim> findById(Long claimId) {
        return claimRepository.findById(claimId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Claim> findByClaimNumber(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Claim> findAllClaims() {
        return claimRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Claim> findClaimsByClaimant(Long claimantId) {
        return claimRepository.findByClaimantUserId(claimantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Claim> findClaimsByAgent(Long agentId) {
        return claimRepository.findByAgentUserId(agentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Claim> findClaimsByAdjuster(Long adjusterId) {
        return claimRepository.findByAdjusterUserId(adjusterId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Claim> findClaimsByPolicy(Long policyId) {
        return claimRepository.findByPolicyPolicyId(policyId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Claim> findClaimsByStatus(ClaimStatus status) {
        return claimRepository.findByClaimStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Claim> findPendingClaims() {
        return claimRepository.findPendingClaims();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Claim> findUnassignedClaims() {
        return claimRepository.findUnassignedClaims();
    }
    
    @Override
    public Claim assignAdjuster(Long claimId, User adjuster) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        claim.setAdjuster(adjuster);
        claim.setClaimStatus(ClaimStatus.UNDER_REVIEW);
        
        return claimRepository.save(claim);
    }
    
    @Override
    public Claim reviewClaim(Long claimId, ClaimReviewDTO reviewDTO, User adjuster) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        claim.setAdjuster(adjuster);
        claim.setClaimStatus(reviewDTO.getClaimStatus());
        claim.setApprovedAmount(reviewDTO.getApprovedAmount());
        claim.setRemarks(reviewDTO.getRemarks());
        
        return claimRepository.save(claim);
    }
    
    @Override
    public Claim updateClaimStatus(Long claimId, ClaimStatus status) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        claim.setClaimStatus(status);
        return claimRepository.save(claim);
    }
    
    @Override
    public ClaimDTO convertToDTO(Claim claim) {
        return ClaimDTO.builder()
                .claimId(claim.getClaimId())
                .claimNumber(claim.getClaimNumber())
                .policyId(claim.getPolicy().getPolicyId())
                .policyName(claim.getPolicy().getPolicyName())
                .policyNumber(claim.getPolicy().getPolicyNumber())
                .claimantId(claim.getClaimant().getUserId())
                .claimantName(claim.getClaimant().getFullName())
                .agentId(claim.getAgent() != null ? claim.getAgent().getUserId() : null)
                .agentName(claim.getAgent() != null ? claim.getAgent().getFullName() : null)
                .adjusterId(claim.getAdjuster() != null ? claim.getAdjuster().getUserId() : null)
                .adjusterName(claim.getAdjuster() != null ? claim.getAdjuster().getFullName() : null)
                .claimAmount(claim.getClaimAmount())
                .approvedAmount(claim.getApprovedAmount())
                .coverageAmount(claim.getPolicy().getCoverageAmount())
                .claimDate(claim.getClaimDate())
                .description(claim.getDescription())
                .reason(claim.getReason())
                .claimStatus(claim.getClaimStatus())
                .remarks(claim.getRemarks())
                .createdDate(claim.getCreatedDate() != null ? 
                        claim.getCreatedDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .build();
    }
    
    @Override
    public List<ClaimDTO> convertToDTOList(List<Claim> claims) {
        return claims.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public String generateClaimNumber() {
        String prefix = "CLM";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + "-" + datePart + "-" + uniquePart;
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countClaimsByStatus(ClaimStatus status) {
        return claimRepository.countByClaimStatus(status);
    }
    
    @Override
    public Claim updateClaim(Long claimId, ClaimDTO claimDTO) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        // Only allow updates to pending claims
        if (claim.getClaimStatus() != ClaimStatus.PENDING) {
            throw new RuntimeException("Only pending claims can be updated");
        }
        
        claim.setClaimAmount(claimDTO.getClaimAmount());
        claim.setDescription(claimDTO.getDescription());
        claim.setReason(claimDTO.getReason());
        
        return claimRepository.save(claim);
    }
}
