package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.edu.dto.StudentDTO;
import org.edu.entity.Student;
import org.edu.entity.User;
import org.edu.exception.InvalidStudentDataException;
import org.edu.exception.ResourceNotFoundException;
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
        // Arrange
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

        // Act & Assert
        assertThrows(InvalidStudentDataException.class,
                () -> studentService.createStudent(dto)
        );

        verify(studentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectStudentCreationWhenUserAlreadyAssigned() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setRole(Role.STUDENT);

        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(studentRepository.existsByUser(user)).thenReturn(true);

        // Act & Assert
        assertThrows(InvalidStudentDataException.class,
                () -> studentService.createStudent(studentDTO)
        );

        verify(studentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldSoftDeleteActiveStudent() {
        Student student = new Student();
        student.setId(1L);
        student.setActive(true);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        studentService.deleteStudent(1L);

        assertFalse(student.isActive());
    }
}
