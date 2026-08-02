package org.edu.repository;

import org.edu.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject,Long>, JpaSpecificationExecutor<Subject> {
    Page<Subject> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    Optional<Subject> findByCode(String code);

}
