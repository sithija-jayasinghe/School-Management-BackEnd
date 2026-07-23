package org.edu.repository;

import org.edu.entity.Class;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassRepository extends JpaRepository<Class, Long> {

    Optional<Class> findByIdAndActiveTrue(Long id);

    Optional<Class> findByAcademicYearIdAndGradeIdAndSection(
            Long academicYearId,
            Long gradeId,
            String section
    );

    List<Class> findByActiveTrue();

    Page<Class> findByActiveTrue(Pageable pageable);

    @Query("""
            select clazz
            from Class clazz
            left join clazz.grade grade
            where clazz.active = true
              and (
                lower(clazz.name) like lower(concat('%', :keyword, '%'))
                or lower(clazz.section) like lower(concat('%', :keyword, '%'))
                or lower(grade.name) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<Class> searchActiveClasses(@Param("keyword") String keyword, Pageable pageable);

    List<Class> findByGradeIdInAndActiveTrue(List<Long> gradeIds);

    @Query("""
            select distinct clazz
            from Class clazz
            join clazz.subjects subject
            where subject.id = :subjectId
            """)
    List<Class> findClassesLinkedToSubject(@Param("subjectId") Long subjectId);

    List<Class> findByClassTeacherIdAndActiveTrueOrderByNameAsc(Long classTeacherId);

    boolean existsByIdAndClassTeacherIdAndActiveTrue(Long id, Long classTeacherId);

    boolean existsByAcademicYearIdAndGradeIdAndSectionAndActiveTrue(
            Long academicYearId,
            Long gradeId,
            String section
    );

    boolean existsByAcademicYearIdAndGradeIdAndSectionAndIdNotAndActiveTrue(
            Long academicYearId,
            Long gradeId,
            String section,
            Long id
    );
}
