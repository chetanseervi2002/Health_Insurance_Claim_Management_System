package com.hicms.repository;

import com.hicms.entity.Claim;
import com.hicms.entity.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Claim entity
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    
    Optional<Claim> findByClaimNumber(String claimNumber);
    
    boolean existsByClaimNumber(String claimNumber);
    
    List<Claim> findByClaimStatus(ClaimStatus status);
    
    List<Claim> findByClaimStatusIn(List<ClaimStatus> statuses);
    
    List<Claim> findByClaimantUserId(Long claimantId);
    
    List<Claim> findByAgentUserId(Long agentId);
    
    List<Claim> findByAdjusterUserId(Long adjusterId);
    
    List<Claim> findByPolicyPolicyId(Long policyId);
    
    @Query("SELECT c FROM Claim c WHERE c.claimStatus = 'PENDING' OR c.claimStatus = 'UNDER_REVIEW'")
    List<Claim> findPendingClaims();
    
    @Query("SELECT c FROM Claim c WHERE c.claimant.userId = :userId ORDER BY c.createdDate DESC")
    List<Claim> findClaimsByClaimantOrderByDate(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Claim c WHERE c.adjuster IS NULL AND c.claimStatus = 'PENDING'")
    List<Claim> findUnassignedClaims();
    
    @Query("SELECT COUNT(c) FROM Claim c WHERE c.claimStatus = :status")
    long countByClaimStatus(@Param("status") ClaimStatus status);
}
