package com.hicms.controller;

import com.hicms.entity.Document;
import com.hicms.entity.User;
import com.hicms.service.DocumentService;
import com.hicms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for Document Management
 */
@Controller
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentService documentService;
    private final UserService userService;
    
    @GetMapping("/claim/{claimId}")
    public String showUploadForm(@PathVariable Long claimId, Model model) {
        model.addAttribute("claimId", claimId);
        return "document/upload";
    }
    
    @PostMapping("/upload")
    public String uploadDocument(@RequestParam Long claimId,
                                  @RequestParam("file") MultipartFile file,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload");
                return "redirect:/claims/view/" + claimId;
            }
            
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            documentService.uploadDocument(claimId, file, user);
            redirectAttributes.addFlashAttribute("successMessage", "Document uploaded successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload document: " + e.getMessage());
        }
        return "redirect:/claims/view/" + claimId;
    }
    
    @PostMapping("/upload/{claimId}")
    public String uploadDocumentPath(@PathVariable Long claimId,
                                  @RequestParam("file") MultipartFile file,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        return uploadDocument(claimId, file, userDetails, redirectAttributes);
    }
    
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        Document document = documentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        byte[] content = documentService.getDocumentContent(id);
        ByteArrayResource resource = new ByteArrayResource(content);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .contentLength(content.length)
                .body(resource);
    }
    
    @PostMapping("/delete/{id}")
    public String deleteDocument(@PathVariable Long id,
                                  @RequestParam Long claimId,
                                  RedirectAttributes redirectAttributes) {
        try {
            documentService.deleteDocument(id);
            redirectAttributes.addFlashAttribute("successMessage", "Document deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete document: " + e.getMessage());
        }
        return "redirect:/claims/view/" + claimId;
    }
}
