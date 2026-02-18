package com.hicms.controller.api;

import com.hicms.dto.PolicyEnrollmentDTO;
import com.hicms.entity.PolicyEnrollment;
import com.hicms.service.PolicyEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST API Controller for Enrollment data
 */


@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentApiController {
    
    private final PolicyEnrollmentService enrollmentService;
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<PolicyEnrollmentDTO>> getEnrollmentsByUser(@PathVariable Long userId) {
        List<PolicyEnrollment> enrollments = enrollmentService.findActiveEnrollmentsByUser(userId);
        List<PolicyEnrollmentDTO> dtos = enrollmentService.convertToDTOList(enrollments);
        return ResponseEntity.ok(dtos);
    }
}
