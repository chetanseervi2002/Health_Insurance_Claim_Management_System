package com.hicms.service.impl;

import com.hicms.dto.PolicyEnrollmentDTO;
import com.hicms.entity.Policy;
import com.hicms.entity.PolicyEnrollment;
import com.hicms.entity.PolicyEnrollment.EnrollmentStatus;
import com.hicms.entity.User;
import com.hicms.repository.PolicyEnrollmentRepository;
import com.hicms.repository.PolicyRepository;
import com.hicms.service.PolicyEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of PolicyEnrollmentService
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PolicyEnrollmentServiceImpl implements PolicyEnrollmentService {
    
    private final PolicyEnrollmentRepository enrollmentRepository;
    private final PolicyRepository policyRepository;
    
    @Override
    public PolicyEnrollment enrollInPolicy(Long policyId, User policyholder, User agent) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        
        // Check if user is already enrolled in an active enrollment
        boolean alreadyEnrolled = enrollmentRepository
                .existsByPolicyholderUserIdAndPolicyPolicyIdAndEnrollmentStatusIn(
                        policyholder.getUserId(), 
                        policyId, 
                        Arrays.asList(EnrollmentStatus.ACTIVE, EnrollmentStatus.PENDING)
                );
        
        if (alreadyEnrolled) {
            throw new RuntimeException("User is already enrolled in this policy");
        }
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = policy.getDurationMonths() != null ? 
                startDate.plusMonths(policy.getDurationMonths()) : null;
        
        PolicyEnrollment enrollment = PolicyEnrollment.builder()
                .policy(policy)
                .policyholder(policyholder)
                .agent(agent)
                .enrollmentDate(LocalDate.now())
                .startDate(startDate)
                .endDate(endDate)
                .enrollmentStatus(EnrollmentStatus.ACTIVE)
                .build();
        
        return enrollmentRepository.save(enrollment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<PolicyEnrollment> findById(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PolicyEnrollment> findByPolicyholder(Long policyholderId) {
        return enrollmentRepository.findByPolicyholderUserId(policyholderId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PolicyEnrollment> findByAgent(Long agentId) {
        return enrollmentRepository.findByAgentUserId(agentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PolicyEnrollment> findByPolicy(Long policyId) {
        return enrollmentRepository.findByPolicyPolicyId(policyId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PolicyEnrollment> findActiveEnrollmentsByUser(Long userId) {
        return enrollmentRepository.findActiveEnrollmentsByUser(userId);
    }
    
    @Override
    public PolicyEnrollment updateEnrollmentStatus(Long enrollmentId, EnrollmentStatus status) {
        PolicyEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        enrollment.setEnrollmentStatus(status);
        return enrollmentRepository.save(enrollment);
    }
    
    @Override
    public void cancelEnrollment(Long enrollmentId) {
        PolicyEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        enrollment.setEnrollmentStatus(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isUserEnrolledInPolicy(Long userId, Long policyId) {
        return enrollmentRepository.existsByPolicyholderUserIdAndPolicyPolicyIdAndEnrollmentStatusIn(
                userId, policyId, Arrays.asList(EnrollmentStatus.ACTIVE, EnrollmentStatus.PENDING));
    }
    
    @Override
    public PolicyEnrollmentDTO convertToDTO(PolicyEnrollment enrollment) {
        return PolicyEnrollmentDTO.builder()
                .enrollmentId(enrollment.getEnrollmentId())
                .policyId(enrollment.getPolicy().getPolicyId())
                .policyName(enrollment.getPolicy().getPolicyName())
                .policyNumber(enrollment.getPolicy().getPolicyNumber())
                .policyholderId(enrollment.getPolicyholder().getUserId())
                .policyholderName(enrollment.getPolicyholder().getFullName())
                .agentId(enrollment.getAgent() != null ? enrollment.getAgent().getUserId() : null)
                .agentName(enrollment.getAgent() != null ? enrollment.getAgent().getFullName() : null)
                .enrollmentDate(enrollment.getEnrollmentDate())
                .startDate(enrollment.getStartDate())
                .endDate(enrollment.getEndDate())
                .enrollmentStatus(enrollment.getEnrollmentStatus())
                .coverageAmount(enrollment.getPolicy().getCoverageAmount().toString())
                .premiumAmount(enrollment.getPolicy().getPremiumAmount().toString())
                .build();
    }
    
    @Override
    public List<PolicyEnrollmentDTO> convertToDTOList(List<PolicyEnrollment> enrollments) {
        return enrollments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PolicyEnrollment> findAllEnrollments() {
        return enrollmentRepository.findAll();
    }
    
    @Override
    public void deleteEnrollment(Long enrollmentId) {
        PolicyEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        
        if (enrollment.getEnrollmentStatus() != EnrollmentStatus.CANCELLED) {
            throw new RuntimeException("Only cancelled enrollments can be deleted");
        }
        
        enrollmentRepository.delete(enrollment);
    }
}
