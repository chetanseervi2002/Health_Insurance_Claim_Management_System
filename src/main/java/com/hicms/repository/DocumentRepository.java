package com.hicms.repository;

import com.hicms.entity.Document;
import com.hicms.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Document entity
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByClaimClaimId(Long claimId);
    
    List<Document> findByDocumentType(DocumentType documentType);
    
    List<Document> findByUploadedByUserId(Long userId);
    
    void deleteByClaimClaimId(Long claimId);
}
