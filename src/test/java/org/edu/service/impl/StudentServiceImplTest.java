package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.edu.dto.StudentDTO;
import org.edu.entity.AcademicYear;
import org.edu.entity.StudentEnrollment;
import org.edu.entity.Student;
import org.edu.entity.User;
import org.edu.exception.InvalidStudentDataException;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.StudentMapper;
import org.edu.repository.AcademicYearRepository;
import org.edu.repository.ClassRepository;
import org.edu.repository.ParentRepository;
import org.edu.repository.ParentStudentRepository;
import org.edu.repository.StudentEnrollmentRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.UserRepository;
import org.edu.util.EnrollmentStatus;
import org.edu.util.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AcademicYearRepository academicYearRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private ParentStudentRepository parentStudentRepository;

    @Mock
    private StudentEnrollmentRepository studentEnrollmentRepository;

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

    @Test
    void shouldTransferStudentAndKeepEnrollmentHistory() {
        Student student = activeStudent(1L);
        org.edu.entity.Class oldClass = activeClass(10L, "Grade 5A");
        org.edu.entity.Class newClass = activeClass(11L, "Grade 5B");
        AcademicYear academicYear = currentAcademicYear();
        StudentEnrollment activeEnrollment = activeEnrollment(student, oldClass, academicYear);

        when(studentRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(student));
        when(classRepository.findByIdAndActiveTrue(11L)).thenReturn(Optional.of(newClass));
        when(academicYearRepository.findByCurrentTrueAndActiveTrue()).thenReturn(Optional.of(academicYear));
        when(studentEnrollmentRepository.findByStudentIdAndAcademicYearIdAndStatusOrderByStartDateDesc(
                1L,
                100L,
                EnrollmentStatus.ACTIVE
        )).thenReturn(List.of(activeEnrollment));
        when(studentMapper.toDTO(student)).thenReturn(new StudentDTO());
        when(parentStudentRepository.findByStudentId(1L)).thenReturn(List.of());
        when(studentEnrollmentRepository.findByStudentIdAndStatusOrderByAcademicYearStartDateDesc(
                1L,
                EnrollmentStatus.ACTIVE
        )).thenReturn(List.of(activeEnrollment(student, newClass, academicYear)));

        studentService.transferStudent(1L, 11L);

        assertEquals(EnrollmentStatus.TRANSFERRED, activeEnrollment.getStatus());
        assertEquals(newClass, student.getCurrentClass());
        verify(studentEnrollmentRepository).save(org.mockito.ArgumentMatchers.any(StudentEnrollment.class));
        verify(studentRepository).save(student);
    }

    @Test
    void shouldRejectTransferToSameClass() {
        Student student = activeStudent(1L);
        org.edu.entity.Class currentClass = activeClass(10L, "Grade 5A");
        AcademicYear academicYear = currentAcademicYear();
        StudentEnrollment activeEnrollment = activeEnrollment(student, currentClass, academicYear);

        when(studentRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(student));
        when(classRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(currentClass));
        when(academicYearRepository.findByCurrentTrueAndActiveTrue()).thenReturn(Optional.of(academicYear));
        when(studentEnrollmentRepository.findByStudentIdAndAcademicYearIdAndStatusOrderByStartDateDesc(
                1L,
                100L,
                EnrollmentStatus.ACTIVE
        )).thenReturn(List.of(activeEnrollment));

        assertThrows(IllegalStateException.class, () -> studentService.transferStudent(1L, 10L));
        verify(studentEnrollmentRepository, never()).save(org.mockito.ArgumentMatchers.any(StudentEnrollment.class));
    }

    @Test
    void shouldWithdrawStudentAndClearCurrentClass() {
        Student student = activeStudent(1L);
        org.edu.entity.Class currentClass = activeClass(10L, "Grade 5A");
        AcademicYear academicYear = currentAcademicYear();
        StudentEnrollment activeEnrollment = activeEnrollment(student, currentClass, academicYear);
        student.setCurrentClass(currentClass);

        when(studentRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(student));
        when(academicYearRepository.findByCurrentTrueAndActiveTrue()).thenReturn(Optional.of(academicYear));
        when(studentEnrollmentRepository.findByStudentIdAndAcademicYearIdAndStatusOrderByStartDateDesc(
                1L,
                100L,
                EnrollmentStatus.ACTIVE
        )).thenReturn(List.of(activeEnrollment));
        when(studentMapper.toDTO(student)).thenReturn(new StudentDTO());
        when(parentStudentRepository.findByStudentId(1L)).thenReturn(List.of());
        when(studentEnrollmentRepository.findByStudentIdAndStatusOrderByAcademicYearStartDateDesc(
                1L,
                EnrollmentStatus.ACTIVE
        )).thenReturn(List.of());

        studentService.withdrawStudent(1L);

        assertEquals(EnrollmentStatus.WITHDRAWN, activeEnrollment.getStatus());
        assertNull(student.getCurrentClass());
        verify(studentRepository).save(student);
    }

    @Test
    void shouldCloseDuplicateActiveEnrollmentsWhenPromotingStudent() {
        Student student = activeStudent(1L);
        org.edu.entity.Class firstClass = activeClass(10L, "Grade 5A");
        org.edu.entity.Class secondClass = activeClass(11L, "Grade 5B");
        org.edu.entity.Class promotedClass = activeClass(12L, "Grade 6A");
        AcademicYear academicYear = currentAcademicYear();
        StudentEnrollment firstActiveEnrollment = activeEnrollment(student, firstClass, academicYear);
        StudentEnrollment secondActiveEnrollment = activeEnrollment(student, secondClass, academicYear);
        secondActiveEnrollment.setId(201L);

        when(studentRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(student));
        when(classRepository.findByIdAndActiveTrue(12L)).thenReturn(Optional.of(promotedClass));
        when(academicYearRepository.findByCurrentTrueAndActiveTrue()).thenReturn(Optional.of(academicYear));
        when(studentEnrollmentRepository.findByStudentIdAndAcademicYearIdAndStatusOrderByStartDateDesc(
                1L,
                100L,
                EnrollmentStatus.ACTIVE
        )).thenReturn(List.of(firstActiveEnrollment, secondActiveEnrollment));
        when(studentMapper.toDTO(student)).thenReturn(new StudentDTO());
        when(parentStudentRepository.findByStudentId(1L)).thenReturn(List.of());
        when(studentEnrollmentRepository.findByStudentIdAndStatusOrderByAcademicYearStartDateDesc(
                1L,
                EnrollmentStatus.ACTIVE
        )).thenReturn(List.of(activeEnrollment(student, promotedClass, academicYear)));

        studentService.promoteStudent(1L, 12L);

        assertEquals(EnrollmentStatus.PROMOTED, firstActiveEnrollment.getStatus());
        assertEquals(EnrollmentStatus.PROMOTED, secondActiveEnrollment.getStatus());
        assertEquals(promotedClass, student.getCurrentClass());
        verify(studentEnrollmentRepository).save(org.mockito.ArgumentMatchers.any(StudentEnrollment.class));
    }

    private Student activeStudent(Long id) {
        Student student = new Student();
        student.setId(id);
        student.setName("Kamal Perera");
        student.setDateOfBirth(LocalDate.of(2012, 4, 5));
        student.setPhoneNumber("0771234567");
        student.setActive(true);
        return student;
    }

    private org.edu.entity.Class activeClass(Long id, String name) {
        org.edu.entity.Class clazz = new org.edu.entity.Class();
        clazz.setId(id);
        clazz.setName(name);
        clazz.setActive(true);
        return clazz;
    }

    private AcademicYear currentAcademicYear() {
        AcademicYear academicYear = new AcademicYear();
        academicYear.setId(100L);
        academicYear.setName("2026");
        academicYear.setCurrent(true);
        academicYear.setActive(true);
        return academicYear;
    }

    private StudentEnrollment activeEnrollment(Student student, org.edu.entity.Class clazz, AcademicYear academicYear) {
        StudentEnrollment enrollment = new StudentEnrollment();
        enrollment.setId(200L);
        enrollment.setStudent(student);
        enrollment.setStudentClass(clazz);
        enrollment.setAcademicYear(academicYear);
        enrollment.setStartDate(LocalDate.of(2026, 1, 1));
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        return enrollment;
    }
}
