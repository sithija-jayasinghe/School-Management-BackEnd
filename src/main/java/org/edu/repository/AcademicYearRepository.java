package org.edu.repository;

import org.edu.entity.AcademicYear;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {

    Optional<AcademicYear> findByIdAndActiveTrue(Long id);

    Optional<AcademicYear> findByCurrentTrueAndActiveTrue();

    Page<AcademicYear> findByActiveTrue(Pageable pageable);

    List<AcademicYear> findByActiveTrue();

    Page<AcademicYear> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    @Query("""
            select academicYear
            from AcademicYear academicYear
            where (:keyword is null or lower(academicYear.name) like lower(concat('%', :keyword, '%')))
              and (:active is null or academicYear.active = :active)
              and (:current is null or academicYear.current = :current)
            """)
    Page<AcademicYear> filterAcademicYears(
            @Param("keyword") String keyword,
            @Param("active") Boolean active,
            @Param("current") Boolean current,
            Pageable pageable
    );

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate endDate, LocalDate startDate);

    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
            LocalDate endDate,
            LocalDate startDate,
            Long id
    );
}
