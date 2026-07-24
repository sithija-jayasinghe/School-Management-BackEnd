package org.edu.service;

import org.edu.dto.AcademicCalendarSummaryDTO;
import org.edu.dto.AcademicYearDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AcademicYearService {

    AcademicYearDTO createAcademicYear(AcademicYearDTO academicYearDTO);

    AcademicYearDTO updateAcademicYear(Long id, AcademicYearDTO academicYearDTO);

    void deleteAcademicYear(Long id);

    void activateAcademicYear(Long id);

    void closeAcademicYear(Long id);

    Page<AcademicYearDTO> getAllAcademicYears(Pageable pageable);

    Page<AcademicYearDTO> filterAcademicYears(String keyword, String status, Boolean current, Pageable pageable);

    AcademicCalendarSummaryDTO getAcademicCalendarSummary();

    AcademicYearDTO getAcademicYearById(Long id);

    Page<AcademicYearDTO> searchAcademicYears(String name, Pageable pageable);

    List<AcademicYearDTO> getAllActiveAcademicYears();

    AcademicYearDTO getCurrentAcademicYear();

    AcademicYearDTO setCurrentAcademicYear(Long id);

    boolean overlapsExistingAcademicYear(LocalDate startDate, LocalDate endDate);
}
