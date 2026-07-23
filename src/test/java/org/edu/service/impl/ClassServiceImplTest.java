package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.edu.dto.ClassDTO;
import org.edu.entity.AcademicYear;
import org.edu.entity.Class;
import org.edu.entity.Grade;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.ClassMapper;
import org.edu.repository.AcademicYearRepository;
import org.edu.repository.ClassRepository;
import org.edu.repository.GradeRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.SubjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClassServiceImplTest {

    @Mock
    private ClassRepository classRepository;

    @Mock
    private ClassMapper classMapper;

    @Mock
    private AcademicYearRepository academicYearRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private ClassServiceImpl classService;

    @Test
    void shouldSoftDeleteActiveClass() {
        Class clazz = new Class();
        clazz.setId(1L);
        clazz.setActive(true);

        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));

        classService.deleteClass(1L);

        assertFalse(clazz.isActive());
        verify(classRepository).save(clazz);
    }

    @Test
    void shouldOnlyUpdateActiveClass() {
        ClassDTO dto = new ClassDTO();
        dto.setName("Grade 10");

        when(classRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> classService.updateClass(1L, dto));
        verify(classMapper, never()).updateEntityFromDTO(dto, null);
    }

    @Test
    void shouldReturnOnlyPrimaryClassesForCurrentAcademicYear() {
        AcademicYear currentYear = new AcademicYear();
        currentYear.setId(100L);
        currentYear.setCurrent(true);
        currentYear.setActive(true);

        Class primaryClass = activeClass(1L, "Grade 1 A", 1, "A");
        Class invalidSectionClass = activeClass(2L, "Grade 1 Demo", 1, "Demo");
        Class secondaryClass = activeClass(3L, "Grade 6 A", 6, "A");
        ClassDTO mapped = new ClassDTO();
        mapped.setId(1L);

        when(academicYearRepository.findByCurrentTrueAndActiveTrue()).thenReturn(Optional.of(currentYear));
        when(classRepository.findByAcademicYearIdAndActiveTrueOrderByNameAsc(100L))
                .thenReturn(List.of(primaryClass, invalidSectionClass, secondaryClass));
        when(classMapper.toDTO(primaryClass)).thenReturn(mapped);

        List<ClassDTO> result = classService.getCurrentAcademicYearActiveClasses();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    private Class activeClass(Long id, String name, int gradeLevel, String section) {
        Grade grade = new Grade();
        grade.setId((long) gradeLevel);
        grade.setName("Grade " + gradeLevel);
        grade.setLevel(gradeLevel);
        grade.setActive(true);

        Class clazz = new Class();
        clazz.setId(id);
        clazz.setName(name);
        clazz.setGrade(grade);
        clazz.setSection(section);
        clazz.setActive(true);
        return clazz;
    }
}
