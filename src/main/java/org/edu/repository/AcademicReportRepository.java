package org.edu.repository;

import java.util.Optional;
import org.edu.entity.AcademicReport;
import org.edu.util.AcademicReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademicReportRepository extends JpaRepository<AcademicReport, Long> {

    boolean existsByStudentIdAndAcademicTermId(Long studentId, Long academicTermId);

    Optional<AcademicReport> findByIdAndStatus(Long id, AcademicReportStatus status);

    @Query("""
            select distinct report
            from AcademicReport report
            join fetch report.student student
            join fetch report.studentClass studentClass
            join fetch report.academicTerm term
            join fetch term.academicYear academicYear
            join fetch report.generatedBy generatedBy
            left join fetch report.publishedBy publishedBy
            left join fetch report.subjects subjects
            where report.id = :id
            """)
    Optional<AcademicReport> findDetailById(@Param("id") Long id);

    Page<AcademicReport> findByStudentIdOrderByAcademicTermStartDateDesc(Long studentId, Pageable pageable);

    Page<AcademicReport> findByStudentClassIdAndAcademicTermIdOrderByStudentNameAsc(
            Long classId,
            Long academicTermId,
            Pageable pageable
    );

    Page<AcademicReport> findByStudentIdAndStatusOrderByAcademicTermStartDateDesc(
            Long studentId,
            AcademicReportStatus status,
            Pageable pageable
    );
}
