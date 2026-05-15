package org.edu.repository;

import org.edu.entity.ParentStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {
    boolean existsByParentIdAndStudentId(Long parentId, Long studentId);
    Optional<ParentStudent> findByParentIdAndStudentId(Long parentId, Long studentId);
    List<ParentStudent> findByParentId(Long parentId);
    List<ParentStudent> findByStudentId(Long studentId);
}
