package org.edu.repository;

import java.util.List;
import java.util.Optional;
import org.edu.entity.Exam;
import org.edu.util.ExamType;
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
            select exam
            from Exam exam
            where (:keyword is null
                    or lower(exam.name) like lower(concat('%', :keyword, '%'))
                    or lower(exam.subject.name) like lower(concat('%', :keyword, '%'))
                    or lower(exam.studentClass.name) like lower(concat('%', :keyword, '%')))
              and (:academicYearId is null or exam.academicYear.id = :academicYearId)
              and (:academicTermId is null or exam.academicTerm.id = :academicTermId)
              and (:classId is null or exam.studentClass.id = :classId)
              and (:subjectId is null or exam.subject.id = :subjectId)
              and (:type is null or exam.type = :type)
              and (:active is null or exam.active = :active)
              and (:fromDate is null or exam.examDate >= :fromDate)
              and (:toDate is null or exam.examDate <= :toDate)
            """)
    Page<Exam> filterExams(
            @Param("keyword") String keyword,
            @Param("academicYearId") Long academicYearId,
            @Param("academicTermId") Long academicTermId,
            @Param("classId") Long classId,
            @Param("subjectId") Long subjectId,
            @Param("type") ExamType type,
            @Param("active") Boolean active,
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate,
            Pageable pageable
    );

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

    @Query("""
            select distinct e
            from Exam e
            where e.active = true
              and exists (
                  select 1
                  from TeachingAssignment assignment
                  where assignment.active = true
                    and assignment.staff.id = :staffId
                    and assignment.studentClass.id = e.studentClass.id
                    and assignment.subject.id = e.subject.id
              )
            order by e.examDate asc, e.name asc
            """)
    List<Exam> findTeacherPortalExamsByTeachingAssignment(@Param("staffId") Long staffId);

    @Query("""
            select e
            from Exam e
            where e.active = true
              and e.studentClass.classTeacher.id = :staffId
            order by e.examDate asc, e.name asc
            """)
    List<Exam> findTeacherPortalExamsByClassTeacher(@Param("staffId") Long staffId);

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
