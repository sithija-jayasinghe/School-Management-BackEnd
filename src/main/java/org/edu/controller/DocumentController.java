package org.edu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.DocumentDTO;
import org.edu.dto.DocumentFileResponse;
import org.edu.dto.request.DocumentCreateRequest;
import org.edu.dto.request.DocumentUpdateRequest;
import org.edu.security.UserPrincipal;
import org.edu.service.DocumentService;
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
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentDTO uploadDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestPart("metadata") DocumentCreateRequest request,
            @RequestPart("file") MultipartFile file
    ) {
        return documentService.uploadDocument(principal.getUser().getId(), request, file);
    }

    @PatchMapping("/{documentId}")
    public DocumentDTO updateDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentUpdateRequest request
    ) {
        return documentService.updateDocument(principal.getUser().getId(), documentId, request);
    }

    @DeleteMapping("/{documentId}")
    public void deleteDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long documentId
    ) {
        documentService.deleteDocument(principal.getUser().getId(), documentId);
    }

    @GetMapping("/{documentId}")
    public DocumentDTO getDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long documentId
    ) {
        return documentService.getDocument(principal.getUser().getId(), documentId);
    }

    @GetMapping("/students/{studentId}")
    public Page<DocumentDTO> getDocumentsByStudent(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            Pageable pageable
    ) {
        return documentService.getDocumentsByStudent(principal.getUser().getId(), studentId, pageable);
    }

    @GetMapping("/{documentId}/download")
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
