package com.hicms.repository;

import com.hicms.entity.PolicyEnrollment;
import com.hicms.entity.PolicyEnrollment.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PolicyEnrollment entity
 */
@Repository
public interface PolicyEnrollmentRepository extends JpaRepository<PolicyEnrollment, Long> {
    
    List<PolicyEnrollment> findByPolicyholderUserId(Long policyholderId);
    
    List<PolicyEnrollment> findByAgentUserId(Long agentId);
    
    List<PolicyEnrollment> findByPolicyPolicyId(Long policyId);
    
    List<PolicyEnrollment> findByEnrollmentStatus(EnrollmentStatus status);
    
    List<PolicyEnrollment> findByPolicyholderUserIdAndEnrollmentStatus(Long policyholderId, EnrollmentStatus status);
    
    @Query("SELECT pe FROM PolicyEnrollment pe WHERE pe.policyholder.userId = :userId AND pe.policy.policyId = :policyId")
    Optional<PolicyEnrollment> findByPolicyholderAndPolicy(@Param("userId") Long userId, @Param("policyId") Long policyId);
    
    boolean existsByPolicyholderUserIdAndPolicyPolicyIdAndEnrollmentStatusIn(
            Long policyholderId, Long policyId, List<EnrollmentStatus> statuses);
    
    @Query("SELECT pe FROM PolicyEnrollment pe WHERE pe.enrollmentStatus = 'ACTIVE' AND pe.policyholder.userId = :userId")
    List<PolicyEnrollment> findActiveEnrollmentsByUser(@Param("userId") Long userId);
}
