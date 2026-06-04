package org.edu.repository;

import java.util.List;
import java.util.Optional;
import org.edu.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    Optional<Exam> findByIdAndActiveTrue(Long id);

    Page<Exam> findByActiveTrue(Pageable pageable);

    List<Exam> findByAcademicTermIdAndActiveTrueOrderByExamDateAsc(Long academicTermId);

    Page<Exam> findByStudentClassIdAndActiveTrue(Long classId, Pageable pageable);

    Page<Exam> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    boolean existsByAcademicTermIdAndStudentClassIdAndSubjectIdAndNameIgnoreCase(
            Long academicTermId,
            Long classId,
            Long subjectId,
            String name
    );

    boolean existsByAcademicTermIdAndStudentClassIdAndSubjectIdAndNameIgnoreCaseAndIdNot(
            Long academicTermId,
            Long classId,
            Long subjectId,
            String name,
            Long id
    );
}
