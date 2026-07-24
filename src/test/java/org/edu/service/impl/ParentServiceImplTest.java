package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.edu.entity.Parent;
import org.edu.mapper.ParentMapper;
import org.edu.mapper.ParentStudentMapper;
import org.edu.repository.ParentRepository;
import org.edu.repository.ParentStudentRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParentServiceImplTest {

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private ParentMapper parentMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ParentStudentRepository parentStudentRepository;

    @Mock
    private ParentStudentMapper parentStudentMapper;

    @InjectMocks
    private ParentServiceImpl parentService;

    @Test
    void shouldSoftDeleteActiveParent() {
        Parent parent = new Parent();
        parent.setId(1L);
        parent.setActive(true);

        when(parentRepository.findById(1L)).thenReturn(Optional.of(parent));

        parentService.deleteParent(1L);

        assertFalse(parent.isActive());
        verify(parentRepository).save(parent);
    }

    @Test
    void shouldRejectDeletingInactiveParent() {
        Parent parent = new Parent();
        parent.setId(1L);
        parent.setActive(false);

        when(parentRepository.findById(1L)).thenReturn(Optional.of(parent));

        assertThrows(IllegalStateException.class, () -> parentService.deleteParent(1L));
    }

    @Test
    void shouldPersistParentActivationChanges() {
        Parent parent = new Parent();
        parent.setId(1L);
        parent.setActive(false);

        when(parentRepository.findById(1L)).thenReturn(Optional.of(parent));

        parentService.activateParent(1L);

        assertTrue(parent.isActive());
        verify(parentRepository).save(parent);
    }
}
