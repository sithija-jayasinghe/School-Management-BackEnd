package org.edu.repository;

import org.edu.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject,Long> {
    Page<Subject> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    Optional<Subject> findByCode(String code);

    @Query("""
            select distinct subject
            from Subject subject
            left join subject.grades grade
            left join subject.classes studentClass
            where (:keyword is null
                or lower(subject.code) like lower(concat('%', :keyword, '%'))
                or lower(subject.name) like lower(concat('%', :keyword, '%'))
                or lower(subject.description) like lower(concat('%', :keyword, '%')))
              and (:gradeId is null or grade.id = :gradeId)
              and (:hasClassCoverage is null
                or (:hasClassCoverage = true and studentClass.id is not null)
                or (:hasClassCoverage = false and studentClass.id is null))
            """)
    Page<Subject> filterSubjects(
            @Param("keyword") String keyword,
            @Param("gradeId") Long gradeId,
            @Param("hasClassCoverage") Boolean hasClassCoverage,
            Pageable pageable
    );
}
