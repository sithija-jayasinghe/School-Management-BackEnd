package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.DocumentDTO;
import org.edu.dto.DocumentFileResponse;
import org.edu.dto.request.DocumentCreateRequest;
import org.edu.dto.request.DocumentUpdateRequest;
import org.edu.security.UserPrincipal;
import org.edu.service.AuditLogService;
import org.edu.service.DocumentService;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
@Tag(name = "Documents", description = "Upload, manage, and download student documents")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;
    private final AuditLogService auditLogService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document for a student")
    public DocumentDTO uploadDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestPart("metadata") DocumentCreateRequest request,
            @RequestPart("file") MultipartFile file
    ) {
        DocumentDTO response = documentService.uploadDocument(principal.getUser().getId(), request, file);
        auditLogService.log(AuditAction.CREATE, AuditEntityType.DOCUMENT, response.getId(), response.getTitle(), "Uploaded student document");
        return response;
    }

    @PatchMapping(value = "/{documentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update document metadata")
    public DocumentDTO updateDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long documentId,
            @Valid @RequestPart("metadata") DocumentUpdateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        DocumentDTO response = documentService.updateDocument(principal.getUser().getId(), documentId, request, file);
        auditLogService.log(AuditAction.UPDATE, AuditEntityType.DOCUMENT, response.getId(), response.getTitle(), "Updated document metadata");
        return response;
    }

    @DeleteMapping("/{documentId}")
    @Operation(summary = "Delete a document")
    public void deleteDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long documentId
    ) {
        documentService.deleteDocument(principal.getUser().getId(), documentId);
        auditLogService.log(AuditAction.DEACTIVATE, AuditEntityType.DOCUMENT, documentId, "Document #" + documentId, "Deleted document");
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "Get document metadata by id")
    public DocumentDTO getDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long documentId
    ) {
        return documentService.getDocument(principal.getUser().getId(), documentId);
    }

    @GetMapping("/students/{studentId}")
    @Operation(summary = "List documents for a student")
    public Page<DocumentDTO> getDocumentsByStudent(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            Pageable pageable
    ) {
        return documentService.getDocumentsByStudent(principal.getUser().getId(), studentId, pageable);
    }

    @GetMapping("/{documentId}/download")
    @Operation(summary = "Download a document file")
    public ResponseEntity<Resource> downloadDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long documentId
    ) {
        DocumentFileResponse fileResponse = documentService.downloadDocument(principal.getUser().getId(), documentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileResponse.getContentType()))
                .contentLength(fileResponse.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResponse.getFileName() + "\"")
                .body(fileResponse.getResource());
    }
}
