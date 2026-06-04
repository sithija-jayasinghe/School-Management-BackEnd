package org.edu.repository;

import org.edu.entity.Class;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassRepository extends JpaRepository<Class, Long> {

    Optional<Class> findByIdAndActiveTrue(Long id);

    List<Class> findByActiveTrue();

    Page<Class> findByActiveTrue(Pageable pageable);

    Page<Class> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    List<Class> findByClassTeacherIdAndActiveTrueOrderByNameAsc(Long classTeacherId);

    boolean existsByIdAndClassTeacherIdAndActiveTrue(Long id, Long classTeacherId);

}
