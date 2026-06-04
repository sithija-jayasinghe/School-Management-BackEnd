package org.edu.repository;

import java.util.List;
import java.util.Optional;
import org.edu.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    Optional<Exam> findByIdAndActiveTrue(Long id);

    Page<Exam> findByActiveTrue(Pageable pageable);

    List<Exam> findByAcademicTermIdAndActiveTrueOrderByExamDateAsc(Long academicTermId);

    Page<Exam> findByStudentClassIdAndActiveTrue(Long classId, Pageable pageable);

    Page<Exam> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    @Query("""
            select e
            from Exam e
            where e.active = true
              and exists (
                  select 1
                  from Timetable t
                  where t.staff.id = :staffId
                    and t.studentClass.id = e.studentClass.id
                    and t.subject.id = e.subject.id
              )
            order by e.examDate asc, e.name asc
            """)
    List<Exam> findTeacherPortalExamsByStaffId(@Param("staffId") Long staffId);

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
