package com.hicms.service;

import com.hicms.dto.PolicyDTO;
import com.hicms.entity.Policy;
import com.hicms.entity.PolicyStatus;
import com.hicms.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Policy operations
 */
public interface PolicyService {
    
    Policy createPolicy(PolicyDTO policyDTO, User createdBy);
    
    Policy updatePolicy(Long policyId, PolicyDTO policyDTO);
    
    Optional<Policy> findById(Long policyId);
    
    Optional<Policy> findByPolicyNumber(String policyNumber);
    
    List<Policy> findAllPolicies();
    
    List<Policy> findActivePolicies();
    
    List<Policy> findPoliciesByStatus(PolicyStatus status);
    
    void deletePolicy(Long policyId);
    
    void updatePolicyStatus(Long policyId, PolicyStatus status);
    
    PolicyDTO convertToDTO(Policy policy);
    
    String generatePolicyNumber();
    
    List<Policy> searchPolicies(String keyword);
}
