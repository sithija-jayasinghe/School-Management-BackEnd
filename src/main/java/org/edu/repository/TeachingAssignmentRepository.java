package org.edu.repository;

import java.util.List;
import java.util.Optional;
import org.edu.entity.TeachingAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long> {

    Optional<TeachingAssignment> findByIdAndActiveTrue(Long id);

    Page<TeachingAssignment> findByActiveTrue(Pageable pageable);

    List<TeachingAssignment> findByStaffIdAndActiveTrue(Long staffId);

    List<TeachingAssignment> findByStudentClassIdAndActiveTrue(Long classId);

    boolean existsByStaffIdAndStudentClassIdAndActiveTrue(Long staffId, Long classId);

    boolean existsByStaffIdAndStudentClassIdAndSubjectIdAndActiveTrue(Long staffId, Long classId, Long subjectId);

    boolean existsByStaffIdAndStudentClassIdAndSubjectIdAndAcademicYearId(
            Long staffId,
            Long classId,
            Long subjectId,
            Long academicYearId
    );

    boolean existsByStaffIdAndStudentClassIdAndSubjectIdAndAcademicYearIdAndIdNot(
            Long staffId,
            Long classId,
            Long subjectId,
            Long academicYearId,
            Long id
    );

    @Query("""
            select assignment
            from TeachingAssignment assignment
            join assignment.staff staff
            join assignment.studentClass studentClass
            join assignment.subject subject
            where assignment.active = true
              and (
                lower(staff.name) like lower(concat('%', :keyword, '%'))
                or lower(studentClass.name) like lower(concat('%', :keyword, '%'))
                or lower(subject.name) like lower(concat('%', :keyword, '%'))
                or lower(subject.code) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<TeachingAssignment> searchActiveAssignments(@Param("keyword") String keyword, Pageable pageable);
}
