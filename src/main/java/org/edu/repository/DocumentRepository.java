package org.edu.repository;

import java.util.Optional;
import org.edu.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByIdAndActiveTrue(Long id);

    Page<Document> findByStudentIdAndActiveTrueOrderByCreatedAtDesc(Long studentId, Pageable pageable);

    Page<Document> findByStudentIdAndVisibleToParentTrueAndActiveTrueOrderByCreatedAtDesc(Long studentId, Pageable pageable);
}
