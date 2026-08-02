package org.edu.service;

import org.edu.dto.DocumentDTO;
import org.edu.dto.DocumentFileResponse;
import org.edu.dto.request.DocumentCreateRequest;
import org.edu.dto.request.DocumentUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

// DEMO-FEATURE: document-class-type-visibility-filters START
// import org.edu.util.DocumentType;
// DEMO-FEATURE: document-class-type-visibility-filters END

public interface DocumentService {

    DocumentDTO uploadDocument(Long authenticatedUserId, DocumentCreateRequest request, MultipartFile file);

    DocumentDTO updateDocument(Long authenticatedUserId, Long documentId, DocumentUpdateRequest request, MultipartFile file);

    void deleteDocument(Long authenticatedUserId, Long documentId);

    DocumentDTO getDocument(Long authenticatedUserId, Long documentId);

    Page<DocumentDTO> getDocumentsByStudent(Long authenticatedUserId, Long studentId, Pageable pageable);

    // DEMO-FEATURE: document-class-type-visibility-filters START
    // Page<DocumentDTO> filterDocuments(
    //         Long authenticatedUserId,
    //         Long studentId,
    //         Long classId,
    //         DocumentType documentType,
    //         Boolean visibleToParent,
    //         Pageable pageable
    // );
    // DEMO-FEATURE: document-class-type-visibility-filters END

    DocumentFileResponse downloadDocument(Long authenticatedUserId, Long documentId);

    Page<DocumentDTO> getVisibleDocumentsByStudent(Long studentId, Pageable pageable);

    DocumentFileResponse downloadVisibleDocument(Long studentId, Long documentId);
}
