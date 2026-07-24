package org.edu.repository;

import org.edu.entity.AcademicTerm;
import org.edu.entity.AcademicYear;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AcademicTermRepository extends JpaRepository<AcademicTerm, Long> {

    Optional<AcademicTerm> findByIdAndActiveTrue(Long id);

    Optional<AcademicTerm> findByCurrentTrueAndActiveTrue();

    Page<AcademicTerm> findByActiveTrue(Pageable pageable);

    List<AcademicTerm> findByActiveTrue();

    List<AcademicTerm> findByAcademicYearIdAndActiveTrue(Long academicYearId);

    long countByAcademicYearIdAndActiveTrue(Long academicYearId);

    Optional<AcademicTerm> findFirstByAcademicYearIdAndActiveTrueAndStartDateAfterOrderByStartDateAsc(
            Long academicYearId,
            LocalDate startDate
    );

    Optional<AcademicTerm> findByAcademicYearIdAndCurrentTrueAndActiveTrue(Long academicYearId);

    Page<AcademicTerm> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    boolean existsByAcademicYearAndNameIgnoreCase(AcademicYear academicYear, String name);

    boolean existsByAcademicYearAndNameIgnoreCaseAndIdNot(AcademicYear academicYear, String name, Long id);

    boolean existsByAcademicYearAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            AcademicYear academicYear,
            LocalDate endDate,
            LocalDate startDate
    );

    boolean existsByAcademicYearAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
            AcademicYear academicYear,
            LocalDate endDate,
            LocalDate startDate,
            Long id
    );
}
