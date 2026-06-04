package org.edu.repository;

import org.edu.entity.ParentStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {
    boolean existsByParentIdAndStudentId(Long parentId, Long studentId);
    Optional<ParentStudent> findByParentIdAndStudentId(Long parentId, Long studentId);
    List<ParentStudent> findByParentId(Long parentId);
    List<ParentStudent> findByStudentId(Long studentId);

    @Query("""
            select ps
            from ParentStudent ps
            join fetch ps.student student
            left join fetch student.currentClass studentClass
            where ps.parent.id = :parentId
              and student.active = true
            order by student.name asc
            """)
    List<ParentStudent> findActiveStudentLinksByParentId(@Param("parentId") Long parentId);

    @Query("""
            select ps
            from ParentStudent ps
            join fetch ps.student student
            left join fetch student.currentClass studentClass
            left join fetch studentClass.classTeacher
            where ps.parent.id = :parentId
              and student.id = :studentId
              and student.active = true
            """)
    Optional<ParentStudent> findActiveStudentLinkByParentIdAndStudentId(
            @Param("parentId") Long parentId,
            @Param("studentId") Long studentId
    );
}
