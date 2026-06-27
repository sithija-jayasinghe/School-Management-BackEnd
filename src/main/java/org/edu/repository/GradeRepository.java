package org.edu.repository;

import java.util.List;
import java.util.Optional;
import org.edu.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    Optional<Grade> findByIdAndActiveTrue(Long id);

    Optional<Grade> findByLevel(Integer level);

    List<Grade> findByActiveTrueOrderByLevelAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByLevel(Integer level);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByLevelAndIdNot(Integer level, Long id);
}
