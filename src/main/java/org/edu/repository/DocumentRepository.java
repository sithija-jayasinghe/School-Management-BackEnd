package org.edu.repository;

import java.util.Optional;
import org.edu.entity.Document;
import org.edu.util.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    Optional<Document> findByIdAndActiveTrue(Long id);

    Optional<Document> findByIdAndStudentIdAndVisibleToParentTrueAndActiveTrue(Long id, Long studentId);

    Optional<Document> findTopByStudentIdAndUploadedByIdAndDocumentTypeAndTitleAndActiveTrueOrderByCreatedAtDesc(
            Long studentId,
            Long uploadedById,
            DocumentType documentType,
            String title
    );

    Page<Document> findByStudentIdAndActiveTrueOrderByCreatedAtDesc(Long studentId, Pageable pageable);

    Page<Document> findByStudentIdAndVisibleToParentTrueAndActiveTrueOrderByCreatedAtDesc(Long studentId, Pageable pageable);
}
