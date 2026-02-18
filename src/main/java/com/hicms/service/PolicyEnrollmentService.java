package com.hicms.service;

import com.hicms.dto.PolicyEnrollmentDTO;
import com.hicms.entity.PolicyEnrollment;
import com.hicms.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for PolicyEnrollment operations
 */
public interface PolicyEnrollmentService {
    
    PolicyEnrollment enrollInPolicy(Long policyId, User policyholder, User agent);
    
    Optional<PolicyEnrollment> findById(Long enrollmentId);
    
    List<PolicyEnrollment> findByPolicyholder(Long policyholderId);
    
    List<PolicyEnrollment> findByAgent(Long agentId);
    
    List<PolicyEnrollment> findByPolicy(Long policyId);
    
    List<PolicyEnrollment> findActiveEnrollmentsByUser(Long userId);
    
    PolicyEnrollment updateEnrollmentStatus(Long enrollmentId, PolicyEnrollment.EnrollmentStatus status);
    
    void cancelEnrollment(Long enrollmentId);
    
    boolean isUserEnrolledInPolicy(Long userId, Long policyId);
    
    PolicyEnrollmentDTO convertToDTO(PolicyEnrollment enrollment);
    
    List<PolicyEnrollmentDTO> convertToDTOList(List<PolicyEnrollment> enrollments);
    
    List<PolicyEnrollment> findAllEnrollments();
    
    void deleteEnrollment(Long enrollmentId);
}
