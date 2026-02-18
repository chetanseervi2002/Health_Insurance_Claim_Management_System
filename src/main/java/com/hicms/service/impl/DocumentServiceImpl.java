package com.hicms.service.impl;

import com.hicms.dto.DocumentDTO;
import com.hicms.entity.Claim;
import com.hicms.entity.Document;
import com.hicms.entity.DocumentType;
import com.hicms.entity.User;
import com.hicms.repository.ClaimRepository;
import com.hicms.repository.DocumentRepository;
import com.hicms.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of DocumentService
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {
    
    private final DocumentRepository documentRepository;
    private final ClaimRepository claimRepository;
    
    @Value("${app.document.storage.path}")
    private String documentStoragePath;
    
    @Override
    public Document uploadDocument(Long claimId, MultipartFile file, User uploadedBy) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        try {
            // Create storage directory if it doesn't exist
            Path storageDir = Paths.get(documentStoragePath).toAbsolutePath().normalize();
            Files.createDirectories(storageDir);
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + extension;
            
            // Save file
            Path targetLocation = storageDir.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Determine document type
            DocumentType documentType = getDocumentType(extension);
            
            // Create document entity
            Document document = Document.builder()
                    .claim(claim)
                    .documentName(newFilename)
                    .originalFileName(originalFilename)
                    .documentPath(targetLocation.toString())
                    .documentType(documentType)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .uploadedBy(uploadedBy)
                    .build();
            
            return documentRepository.save(document);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Document> findById(Long documentId) {
        return documentRepository.findById(documentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Document> findByClaimId(Long claimId) {
        return documentRepository.findByClaimClaimId(claimId);
    }
    
    @Override
    public void deleteDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        try {
            // Delete physical file
            Path filePath = Paths.get(document.getDocumentPath());
            Files.deleteIfExists(filePath);
            
            // Delete database record
            documentRepository.delete(document);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteDocumentsByClaimId(Long claimId) {
        List<Document> documents = documentRepository.findByClaimClaimId(claimId);
        for (Document document : documents) {
            deleteDocument(document.getDocumentId());
        }
    }
    
    @Override
    public DocumentDTO convertToDTO(Document document) {
        return DocumentDTO.builder()
                .documentId(document.getDocumentId())
                .claimId(document.getClaim().getClaimId())
                .claimNumber(document.getClaim().getClaimNumber())
                .documentName(document.getDocumentName())
                .originalFileName(document.getOriginalFileName())
                .documentPath(document.getDocumentPath())
                .documentType(document.getDocumentType())
                .fileSize(document.getFileSize())
                .contentType(document.getContentType())
                .uploadedByUsername(document.getUploadedBy() != null ? 
                        document.getUploadedBy().getUsername() : null)
                .uploadDate(document.getUploadDate() != null ? 
                        document.getUploadDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .build();
    }
    
    @Override
    public List<DocumentDTO> convertToDTOList(List<Document> documents) {
        return documents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public byte[] getDocumentContent(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        try {
            Path filePath = Paths.get(document.getDocumentPath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public String getDocumentPath(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        return document.getDocumentPath();
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
    
    private DocumentType getDocumentType(String extension) {
        return switch (extension.toUpperCase()) {
            case "PDF" -> DocumentType.PDF;
            case "JPG", "JPEG" -> DocumentType.JPG;
            case "PNG" -> DocumentType.PNG;
            case "DOC" -> DocumentType.DOC;
            case "DOCX" -> DocumentType.DOCX;
            default -> DocumentType.PDF;
        };
    }
}
