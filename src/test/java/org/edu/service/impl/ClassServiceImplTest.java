package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.edu.dto.ClassDTO;
import org.edu.entity.Class;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.ClassMapper;
import org.edu.repository.ClassRepository;
import org.edu.repository.StaffRepository;
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
    private StaffRepository staffRepository;

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
    }

    @Test
    void shouldOnlyUpdateActiveClass() {
        ClassDTO dto = new ClassDTO();
        dto.setName("Grade 10");

        when(classRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> classService.updateClass(1L, dto));
        verify(classMapper, never()).updateEntityFromDTO(dto, null);
    }
}
