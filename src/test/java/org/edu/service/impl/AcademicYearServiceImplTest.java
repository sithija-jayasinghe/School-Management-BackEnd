package org.edu.service.impl;

import org.edu.dto.AcademicYearDTO;
import org.edu.entity.AcademicTerm;
import org.edu.entity.AcademicYear;
import org.edu.entity.Grade;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.AcademicYearMapper;
import org.edu.repository.AcademicTermRepository;
import org.edu.repository.AcademicYearRepository;
import org.edu.repository.ClassRepository;
import org.edu.repository.GradeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademicYearServiceImplTest {

    @Mock
    private AcademicYearRepository academicYearRepository;

    @Mock
    private AcademicTermRepository academicTermRepository;

    @Mock
    private AcademicYearMapper academicYearMapper;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private ClassRepository classRepository;

    @InjectMocks
    private AcademicYearServiceImpl academicYearService;

    @Test
    void shouldRejectAcademicYearWhenStartDateIsAfterEndDate() {
        AcademicYearDTO dto = new AcademicYearDTO();
        dto.setName("2026");
        dto.setStartDate(LocalDate.of(2026, 12, 31));
        dto.setEndDate(LocalDate.of(2026, 1, 1));

        assertThrows(IllegalArgumentException.class, () -> academicYearService.createAcademicYear(dto));
        verify(academicYearRepository, never()).save(org.mockito.Mockito.any(AcademicYear.class));
    }

    @Test
    void shouldRejectOverlappingAcademicYear() {
        AcademicYearDTO dto = new AcademicYearDTO();
        dto.setName("2026");
        dto.setStartDate(LocalDate.of(2026, 1, 1));
        dto.setEndDate(LocalDate.of(2026, 12, 31));

        when(academicYearRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                dto.getEndDate(),
                dto.getStartDate()
        )).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> academicYearService.createAcademicYear(dto));
    }

    @Test
    void shouldNotDeactivateCurrentAcademicYear() {
        AcademicYear academicYear = new AcademicYear();
        academicYear.setId(1L);
        academicYear.setActive(true);
        academicYear.setCurrent(true);

        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(academicYear));

        assertThrows(IllegalStateException.class, () -> academicYearService.deleteAcademicYear(1L));
        assertTrue(academicYear.isActive());
    }

    @Test
    void shouldUnsetPreviousCurrentYearWhenSettingNewCurrentYear() {
        AcademicYear currentYear = new AcademicYear();
        currentYear.setId(1L);
        currentYear.setCurrent(true);
        currentYear.setActive(true);

        AcademicYear selectedYear = new AcademicYear();
        selectedYear.setId(2L);
        selectedYear.setCurrent(false);
        selectedYear.setActive(true);

        AcademicTerm currentTerm = new AcademicTerm();
        currentTerm.setId(10L);
        currentTerm.setCurrent(true);
        currentTerm.setActive(true);
        currentTerm.setAcademicYear(currentYear);

        when(academicYearRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(selectedYear));
        when(academicYearRepository.findByCurrentTrueAndActiveTrue()).thenReturn(Optional.of(currentYear));
        when(academicTermRepository.findByCurrentTrueAndActiveTrue()).thenReturn(Optional.of(currentTerm));
        stubPrimaryClassGeneration();

        academicYearService.setCurrentAcademicYear(2L);

        assertFalse(currentYear.isCurrent());
        assertFalse(currentTerm.isCurrent());
        assertTrue(selectedYear.isCurrent());
    }

    @Test
    void shouldGeneratePrimaryClassesWhenCreatingAcademicYear() {
        AcademicYearDTO request = new AcademicYearDTO();
        request.setName("2027 Academic Year");
        request.setStartDate(LocalDate.of(2027, 1, 1));
        request.setEndDate(LocalDate.of(2027, 12, 31));

        AcademicYear academicYear = new AcademicYear();
        academicYear.setName(request.getName());
        academicYear.setStartDate(request.getStartDate());
        academicYear.setEndDate(request.getEndDate());

        AcademicYear savedAcademicYear = new AcademicYear();
        savedAcademicYear.setId(10L);
        savedAcademicYear.setName(request.getName());
        savedAcademicYear.setStartDate(request.getStartDate());
        savedAcademicYear.setEndDate(request.getEndDate());

        when(academicYearMapper.toEntity(request)).thenReturn(academicYear);
        when(academicYearRepository.save(academicYear)).thenReturn(savedAcademicYear);
        stubPrimaryClassGeneration();

        academicYearService.createAcademicYear(request);

        verify(classRepository, times(25)).save(org.mockito.ArgumentMatchers.any(org.edu.entity.Class.class));
    }

    @Test
    void shouldCloseActiveTermsWhenClosingAcademicYear() {
        AcademicYear academicYear = new AcademicYear();
        academicYear.setId(1L);
        academicYear.setActive(true);
        academicYear.setCurrent(true);

        AcademicTerm currentTerm = new AcademicTerm();
        currentTerm.setId(10L);
        currentTerm.setActive(true);
        currentTerm.setCurrent(true);

        AcademicTerm activeTerm = new AcademicTerm();
        activeTerm.setId(11L);
        activeTerm.setActive(true);

        when(academicYearRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(academicYear));
        when(academicTermRepository.findByAcademicYearIdAndActiveTrue(1L)).thenReturn(List.of(currentTerm, activeTerm));

        academicYearService.closeAcademicYear(1L);

        assertFalse(academicYear.isCurrent());
        assertFalse(academicYear.isActive());
        assertFalse(currentTerm.isCurrent());
        assertFalse(currentTerm.isActive());
        assertFalse(activeTerm.isActive());
    }

    @Test
    void shouldOnlyUpdateActiveAcademicYear() {
        AcademicYearDTO dto = new AcademicYearDTO();
        dto.setName("2026");

        when(academicYearRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> academicYearService.updateAcademicYear(1L, dto));
        verify(academicYearMapper, never()).updateEntityFromDTO(dto, null);
    }

    @Test
    void shouldRejectOverlappingAcademicYearOnUpdate() {
        AcademicYear academicYear = new AcademicYear();
        academicYear.setId(1L);
        academicYear.setName("2026");
        academicYear.setStartDate(LocalDate.of(2026, 1, 1));
        academicYear.setEndDate(LocalDate.of(2026, 12, 31));
        academicYear.setActive(true);

        AcademicYearDTO dto = new AcademicYearDTO();
        dto.setStartDate(LocalDate.of(2026, 6, 1));
        dto.setEndDate(LocalDate.of(2027, 5, 31));

        when(academicYearRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(academicYear));
        when(academicYearRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
                dto.getEndDate(),
                dto.getStartDate(),
                1L
        )).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> academicYearService.updateAcademicYear(1L, dto));
    }

    private void stubPrimaryClassGeneration() {
        when(gradeRepository.findByLevel(org.mockito.ArgumentMatchers.anyInt()))
                .thenAnswer(invocation -> {
                    Integer level = invocation.getArgument(0);
                    Grade grade = new Grade();
                    grade.setId(level.longValue());
                    grade.setName("Grade " + level);
                    grade.setLevel(level);
                    grade.setActive(true);
                    return Optional.of(grade);
                });
        when(classRepository.findByAcademicYearIdAndGradeIdAndSection(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString()
        )).thenReturn(Optional.empty());
    }
}
