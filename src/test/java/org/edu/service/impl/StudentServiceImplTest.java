package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.edu.dto.StudentDTO;
import org.edu.entity.User;
import org.edu.mapper.StudentMapper;
import org.edu.repository.StudentRepository;
import org.edu.repository.UserRepository;
import org.edu.util.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Spy
    private StudentMapper studentMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    @Test
    void shouldRejectStudentCreationWhenUserRoleIsNotStudent() {
        StudentDTO dto = new StudentDTO();
        dto.setUserId(4L);
        dto.setName("Kamal Perera");
        dto.setDateOfBirth(LocalDate.of(2010, 5, 10));
        dto.setPhoneNumber("0771234567");

        User user = new User();
        user.setId(4L);
        user.setRole(Role.ADMIN);

        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(studentRepository.existsByUser(user)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> studentService.createStudent(dto)
        );

        assertEquals("User must have STUDENT role", ex.getMessage());
        verify(studentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectStudentCreationWhenUserAlreadyAssigned() {
        StudentDTO dto = new StudentDTO();
        dto.setUserId(7L);
        dto.setName("Kamal Perera");
        dto.setDateOfBirth(LocalDate.of(2010, 5, 10));
        dto.setPhoneNumber("0771234567");

        User user = new User();
        user.setId(7L);
        user.setRole(Role.STUDENT);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(studentRepository.existsByUser(user)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> studentService.createStudent(dto)
        );

        assertEquals("User already assigned to a student", ex.getMessage());
        verify(studentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
