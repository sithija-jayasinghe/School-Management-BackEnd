package org.edu.service.impl;

import org.edu.dto.AcademicTermDTO;
import org.edu.entity.AcademicTerm;
import org.edu.entity.AcademicYear;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.AcademicTermMapper;
import org.edu.repository.AcademicTermRepository;
import org.edu.repository.AcademicYearRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademicTermServiceImplTest {

    @Mock
    private AcademicTermRepository academicTermRepository;

    @Mock
    private AcademicYearRepository academicYearRepository;

    @Mock
    private AcademicTermMapper academicTermMapper;

    @InjectMocks
    private AcademicTermServiceImpl academicTermService;

    @Test
    void shouldRejectTermOutsideAcademicYearDateRange() {
        AcademicYear academicYear = activeAcademicYear();
        AcademicTermDTO dto = new AcademicTermDTO();
        dto.setAcademicYearId(1L);
        dto.setName("Term 1");
        dto.setStartDate(LocalDate.of(2025, 12, 1));
        dto.setEndDate(LocalDate.of(2026, 3, 31));

        when(academicYearRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(academicYear));

        assertThrows(IllegalArgumentException.class, () -> academicTermService.createAcademicTerm(dto));
        verify(academicTermRepository, never()).save(org.mockito.Mockito.any(AcademicTerm.class));
    }

    @Test
    void shouldRejectOverlappingTermInsideAcademicYear() {
        AcademicYear academicYear = activeAcademicYear();
        AcademicTermDTO dto = new AcademicTermDTO();
        dto.setAcademicYearId(1L);
        dto.setName("Term 1");
        dto.setStartDate(LocalDate.of(2026, 1, 1));
        dto.setEndDate(LocalDate.of(2026, 4, 30));

        when(academicYearRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(academicYear));
        when(academicTermRepository.existsByAcademicYearAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                academicYear,
                dto.getEndDate(),
                dto.getStartDate()
        )).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> academicTermService.createAcademicTerm(dto));
    }

    @Test
    void shouldNotDeactivateCurrentAcademicTerm() {
        AcademicTerm academicTerm = new AcademicTerm();
        academicTerm.setId(1L);
        academicTerm.setActive(true);
        academicTerm.setCurrent(true);

        when(academicTermRepository.findById(1L)).thenReturn(Optional.of(academicTerm));

        assertThrows(IllegalStateException.class, () -> academicTermService.deleteAcademicTerm(1L));
        assertTrue(academicTerm.isActive());
    }

    @Test
    void shouldUnsetPreviousCurrentTermWhenSettingNewCurrentTerm() {
        AcademicYear currentYear = activeAcademicYear();
        currentYear.setCurrent(true);

        AcademicTerm currentTerm = new AcademicTerm();
        currentTerm.setId(1L);
        currentTerm.setCurrent(true);
        currentTerm.setActive(true);

        AcademicTerm selectedTerm = new AcademicTerm();
        selectedTerm.setId(2L);
        selectedTerm.setCurrent(false);
        selectedTerm.setActive(true);
        selectedTerm.setAcademicYear(currentYear);

        when(academicTermRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(selectedTerm));
        when(academicTermRepository.findByCurrentTrueAndActiveTrue()).thenReturn(Optional.of(currentTerm));

        academicTermService.setCurrentAcademicTerm(2L);

        assertFalse(currentTerm.isCurrent());
        assertTrue(selectedTerm.isCurrent());
    }

    @Test
    void shouldRejectCurrentTermWhenAcademicYearIsNotCurrent() {
        AcademicYear nonCurrentYear = activeAcademicYear();
        nonCurrentYear.setCurrent(false);

        AcademicTerm selectedTerm = new AcademicTerm();
        selectedTerm.setId(2L);
        selectedTerm.setCurrent(false);
        selectedTerm.setActive(true);
        selectedTerm.setAcademicYear(nonCurrentYear);

        when(academicTermRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(selectedTerm));

        assertThrows(IllegalStateException.class, () -> academicTermService.setCurrentAcademicTerm(2L));
        verify(academicTermRepository, never()).findByCurrentTrueAndActiveTrue();
        assertFalse(selectedTerm.isCurrent());
    }

    @Test
    void shouldOnlyUpdateActiveAcademicTerm() {
        AcademicTermDTO dto = new AcademicTermDTO();
        dto.setName("Term 1");

        when(academicTermRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> academicTermService.updateAcademicTerm(1L, dto));
        verify(academicTermMapper, never()).updateEntityFromDTO(dto, null);
    }

    @Test
    void shouldRejectOverlappingAcademicTermOnUpdate() {
        AcademicYear academicYear = activeAcademicYear();
        AcademicTerm academicTerm = new AcademicTerm();
        academicTerm.setId(1L);
        academicTerm.setName("Term 1");
        academicTerm.setStartDate(LocalDate.of(2026, 1, 1));
        academicTerm.setEndDate(LocalDate.of(2026, 4, 30));
        academicTerm.setAcademicYear(academicYear);
        academicTerm.setActive(true);

        AcademicTermDTO dto = new AcademicTermDTO();
        dto.setStartDate(LocalDate.of(2026, 3, 1));
        dto.setEndDate(LocalDate.of(2026, 6, 30));

        when(academicTermRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(academicTerm));
        when(academicTermRepository.existsByAcademicYearAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
                academicYear,
                dto.getEndDate(),
                dto.getStartDate(),
                1L
        )).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> academicTermService.updateAcademicTerm(1L, dto));
    }

    private AcademicYear activeAcademicYear() {
        AcademicYear academicYear = new AcademicYear();
        academicYear.setId(1L);
        academicYear.setName("2026");
        academicYear.setStartDate(LocalDate.of(2026, 1, 1));
        academicYear.setEndDate(LocalDate.of(2026, 12, 31));
        academicYear.setActive(true);
        return academicYear;
    }
}
