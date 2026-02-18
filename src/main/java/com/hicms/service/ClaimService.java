package com.hicms.service;

import com.hicms.dto.ClaimDTO;
import com.hicms.dto.ClaimReviewDTO;
import com.hicms.entity.Claim;
import com.hicms.entity.ClaimStatus;
import com.hicms.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Claim operations
 */
public interface ClaimService {
    
    Claim submitClaim(ClaimDTO claimDTO, User claimant);
    
    Claim submitClaimWithAgent(ClaimDTO claimDTO, User claimant, User agent);
    
    Optional<Claim> findById(Long claimId);
    
    Optional<Claim> findByClaimNumber(String claimNumber);
    
    List<Claim> findAllClaims();
    
    List<Claim> findClaimsByClaimant(Long claimantId);
    
    List<Claim> findClaimsByAgent(Long agentId);
    
    List<Claim> findClaimsByAdjuster(Long adjusterId);
    
    List<Claim> findClaimsByPolicy(Long policyId);
    
    List<Claim> findClaimsByStatus(ClaimStatus status);
    
    List<Claim> findPendingClaims();
    
    List<Claim> findUnassignedClaims();
    
    Claim assignAdjuster(Long claimId, User adjuster);
    
    Claim reviewClaim(Long claimId, ClaimReviewDTO reviewDTO, User adjuster);
    
    Claim updateClaimStatus(Long claimId, ClaimStatus status);
    
    ClaimDTO convertToDTO(Claim claim);
    
    List<ClaimDTO> convertToDTOList(List<Claim> claims);
    
    String generateClaimNumber();
    
    long countClaimsByStatus(ClaimStatus status);
    
    Claim updateClaim(Long claimId, ClaimDTO claimDTO);
}
