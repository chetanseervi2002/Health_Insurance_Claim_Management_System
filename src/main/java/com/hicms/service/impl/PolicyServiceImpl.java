package com.hicms.service.impl;

import com.hicms.dto.PolicyDTO;
import com.hicms.entity.Policy;
import com.hicms.entity.PolicyStatus;
import com.hicms.entity.User;
import com.hicms.repository.PolicyRepository;
import com.hicms.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of PolicyService
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PolicyServiceImpl implements PolicyService {
    
    private final PolicyRepository policyRepository;
    
    @Override
    public Policy createPolicy(PolicyDTO policyDTO, User createdBy) {
        Policy policy = Policy.builder()
                .policyNumber(generatePolicyNumber())
                .policyName(policyDTO.getPolicyName())
                .description(policyDTO.getDescription())
                .coverageAmount(policyDTO.getCoverageAmount())
                .premiumAmount(policyDTO.getPremiumAmount())
                .durationMonths(policyDTO.getDurationMonths())
                .policyStatus(PolicyStatus.ACTIVE)
                .createdBy(createdBy)
                .build();
        
        return policyRepository.save(policy);
    }
    
    @Override
    public Policy updatePolicy(Long policyId, PolicyDTO policyDTO) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        
        policy.setPolicyName(policyDTO.getPolicyName());
        policy.setDescription(policyDTO.getDescription());
        policy.setCoverageAmount(policyDTO.getCoverageAmount());
        policy.setPremiumAmount(policyDTO.getPremiumAmount());
        policy.setDurationMonths(policyDTO.getDurationMonths());
        
        if (policyDTO.getPolicyStatus() != null) {
            policy.setPolicyStatus(policyDTO.getPolicyStatus());
        }
        
        return policyRepository.save(policy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Policy> findById(Long policyId) {
        return policyRepository.findById(policyId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Policy> findByPolicyNumber(String policyNumber) {
        return policyRepository.findByPolicyNumber(policyNumber);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Policy> findAllPolicies() {
        return policyRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Policy> findActivePolicies() {
        return policyRepository.findByPolicyStatus(PolicyStatus.ACTIVE);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Policy> findPoliciesByStatus(PolicyStatus status) {
        return policyRepository.findByPolicyStatus(status);
    }
    
    @Override
    public void deletePolicy(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        policy.setPolicyStatus(PolicyStatus.CANCELLED);
        policyRepository.save(policy);
    }
    
    @Override
    public void updatePolicyStatus(Long policyId, PolicyStatus status) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        policy.setPolicyStatus(status);
        policyRepository.save(policy);
    }
    
    @Override
    public PolicyDTO convertToDTO(Policy policy) {
        return PolicyDTO.builder()
                .policyId(policy.getPolicyId())
                .policyNumber(policy.getPolicyNumber())
                .policyName(policy.getPolicyName())
                .description(policy.getDescription())
                .coverageAmount(policy.getCoverageAmount())
                .premiumAmount(policy.getPremiumAmount())
                .durationMonths(policy.getDurationMonths())
                .policyStatus(policy.getPolicyStatus())
                .createdByUsername(policy.getCreatedBy() != null ? policy.getCreatedBy().getUsername() : null)
                .createdDate(policy.getCreatedDate() != null ? 
                        policy.getCreatedDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                .build();
    }
    
    @Override
    public String generatePolicyNumber() {
        String prefix = "POL";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + "-" + datePart + "-" + uniquePart;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Policy> searchPolicies(String keyword) {
        return policyRepository.findByPolicyNameContainingIgnoreCase(keyword);
    }
}
