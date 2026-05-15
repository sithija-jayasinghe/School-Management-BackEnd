package org.edu.repository;

import org.edu.entity.AcademicYear;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {

    Optional<AcademicYear> findByIdAndActiveTrue(Long id);

    Optional<AcademicYear> findByCurrentTrueAndActiveTrue();

    Page<AcademicYear> findByActiveTrue(Pageable pageable);

    List<AcademicYear> findByActiveTrue();

    Page<AcademicYear> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate endDate, LocalDate startDate);

    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
            LocalDate endDate,
            LocalDate startDate,
            Long id
    );
}
