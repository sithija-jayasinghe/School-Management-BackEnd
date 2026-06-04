package org.edu.repository;

import java.util.List;
import java.util.Optional;
import org.edu.entity.StudentMark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentMarkRepository extends JpaRepository<StudentMark, Long> {

    Optional<StudentMark> findByExamIdAndStudentId(Long examId, Long studentId);

    boolean existsByExamIdAndStudentId(Long examId, Long studentId);

    boolean existsByExamIdAndStudentIdAndIdNot(Long examId, Long studentId, Long id);

    Page<StudentMark> findByExamIdOrderByStudentNameAsc(Long examId, Pageable pageable);

    Page<StudentMark> findByStudentIdOrderByExamExamDateDesc(Long studentId, Pageable pageable);

    List<StudentMark> findByExamId(Long examId);

    @Query("""
            select sm
            from StudentMark sm
            join fetch sm.exam exam
            join fetch exam.academicTerm academicTerm
            join fetch exam.subject subject
            where sm.student.id = :studentId
            order by exam.examDate desc, sm.id desc
            """)
    List<StudentMark> findPortalResultsByStudentId(@Param("studentId") Long studentId);
}
