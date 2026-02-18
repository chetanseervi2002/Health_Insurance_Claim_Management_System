package com.hicms.service;

import com.hicms.dto.DocumentDTO;
import com.hicms.entity.Document;
import com.hicms.entity.User;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Document operations
 */
public interface DocumentService {
    
    Document uploadDocument(Long claimId, MultipartFile file, User uploadedBy);
    
    Optional<Document> findById(Long documentId);
    
    List<Document> findByClaimId(Long claimId);
    
    void deleteDocument(Long documentId);
    
    void deleteDocumentsByClaimId(Long claimId);
    
    DocumentDTO convertToDTO(Document document);
    
    List<DocumentDTO> convertToDTOList(List<Document> documents);
    
    byte[] getDocumentContent(Long documentId);
    
    String getDocumentPath(Long documentId);
}
