package com.hicms.repository;

import com.hicms.entity.Policy;
import com.hicms.entity.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Policy entity
 */
@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    
    Optional<Policy> findByPolicyNumber(String policyNumber);
    
    boolean existsByPolicyNumber(String policyNumber);
    
    List<Policy> findByPolicyStatus(PolicyStatus status);
    
    List<Policy> findByPolicyStatusIn(List<PolicyStatus> statuses);
    
    List<Policy> findByCreatedByUserId(Long userId);
    
    List<Policy> findByPolicyNameContainingIgnoreCase(String policyName);
}
