package org.edu.service;

import org.edu.dto.AcademicTermDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AcademicTermService {

    AcademicTermDTO createAcademicTerm(AcademicTermDTO academicTermDTO);

    AcademicTermDTO updateAcademicTerm(Long id, AcademicTermDTO academicTermDTO);

    void deleteAcademicTerm(Long id);

    void activateAcademicTerm(Long id);

    void closeAcademicTerm(Long id);

    Page<AcademicTermDTO> getAllAcademicTerms(Pageable pageable);

    AcademicTermDTO getAcademicTermById(Long id);

    Page<AcademicTermDTO> searchAcademicTerms(String name, Pageable pageable);

    List<AcademicTermDTO> getAllActiveAcademicTerms();

    List<AcademicTermDTO> getTermsByAcademicYear(Long academicYearId);

    List<AcademicTermDTO> getActiveTermsByAcademicYear(Long academicYearId);

    AcademicTermDTO getCurrentAcademicTerm();

    AcademicTermDTO getCurrentAcademicTermByAcademicYear(Long academicYearId);

    AcademicTermDTO setCurrentAcademicTerm(Long id);

    boolean termOverlapsInAcademicYear(Long academicYearId, LocalDate startDate, LocalDate endDate);
}
