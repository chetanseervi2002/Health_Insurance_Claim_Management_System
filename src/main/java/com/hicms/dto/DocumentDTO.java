package com.hicms.dto;

import com.hicms.entity.DocumentType;
import lombok.*;

/**
 * DTO for document information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDTO {
    
    private Long documentId;
    private Long claimId;
    private String claimNumber;
    private String documentName;
    private String originalFileName;
    private String documentPath;
    private DocumentType documentType;
    private Long fileSize;
    private String contentType;
    private String uploadedByUsername;
    private String uploadDate;
}
