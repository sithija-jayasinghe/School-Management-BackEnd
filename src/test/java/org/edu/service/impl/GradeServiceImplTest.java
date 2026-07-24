package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.edu.dto.GradeDTO;
import org.edu.entity.Grade;
import org.edu.exception.ResourceNotFoundException;
import org.edu.repository.GradeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GradeServiceImplTest {

    @Mock
    private GradeRepository gradeRepository;

    @InjectMocks
    private GradeServiceImpl gradeService;

    @Test
    void shouldCreateGradeWhenNameAndLevelAreUnique() {
        GradeDTO request = request("Grade 3", 3, true);

        when(gradeRepository.existsByNameIgnoreCase("Grade 3")).thenReturn(false);
        when(gradeRepository.existsByLevel(3)).thenReturn(false);
        when(gradeRepository.save(org.mockito.ArgumentMatchers.any(Grade.class)))
                .thenAnswer(invocation -> {
                    Grade grade = invocation.getArgument(0);
                    grade.setId(1L);
                    return grade;
                });

        GradeDTO saved = gradeService.createGrade(request);

        assertEquals(1L, saved.getId());
        assertEquals("Grade 3", saved.getName());
        assertEquals(3, saved.getLevel());
    }

    @Test
    void shouldRejectDuplicateGradeLevel() {
        GradeDTO request = request("Grade 3", 3, true);

        when(gradeRepository.existsByNameIgnoreCase("Grade 3")).thenReturn(false);
        when(gradeRepository.existsByLevel(3)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> gradeService.createGrade(request));
        verify(gradeRepository, never()).save(org.mockito.ArgumentMatchers.any(Grade.class));
    }

    @Test
    void shouldRejectNonPrimaryGrade() {
        GradeDTO request = request("Grade 6", 6, true);

        assertThrows(IllegalArgumentException.class, () -> gradeService.createGrade(request));
        verify(gradeRepository, never()).save(org.mockito.ArgumentMatchers.any(Grade.class));
    }

    @Test
    void shouldOnlyUpdateActiveGrade() {
        GradeDTO request = request("Grade 12", 12, true);

        when(gradeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gradeService.updateGrade(1L, request));
        verify(gradeRepository, never()).save(org.mockito.ArgumentMatchers.any(Grade.class));
    }

    @Test
    void shouldDeactivateActiveGrade() {
        Grade grade = grade(1L, "Grade 10", 10, true);

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));

        gradeService.deactivateGrade(1L);

        assertFalse(grade.isActive());
        verify(gradeRepository).save(grade);
    }

    @Test
    void shouldNotDeactivatePrimarySystemGrade() {
        Grade grade = grade(1L, "Grade 1", 1, true);

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));

        assertThrows(IllegalStateException.class, () -> gradeService.deactivateGrade(1L));
        verify(gradeRepository, never()).save(grade);
    }

    private GradeDTO request(String name, Integer level, boolean active) {
        GradeDTO dto = new GradeDTO();
        dto.setName(name);
        dto.setLevel(level);
        dto.setActive(active);
        return dto;
    }

    private Grade grade(Long id, String name, Integer level, boolean active) {
        Grade grade = new Grade();
        grade.setId(id);
        grade.setName(name);
        grade.setLevel(level);
        grade.setActive(active);
        return grade;
    }
}
