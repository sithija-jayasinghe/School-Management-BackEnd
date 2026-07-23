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
import org.edu.dto.StudentParentInlineRequest;
import org.edu.entity.AcademicYear;
import org.edu.entity.Parent;
import org.edu.entity.ParentStudent;
import org.edu.entity.StudentEnrollment;
import org.edu.entity.Student;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.StudentMapper;
import org.edu.repository.AcademicYearRepository;
import org.edu.repository.ClassRepository;
import org.edu.repository.HouseRepository;
import org.edu.repository.ParentRepository;
import org.edu.repository.ParentStudentRepository;
import org.edu.repository.StudentEnrollmentRepository;
import org.edu.repository.StudentRepository;
import org.edu.util.EnrollmentStatus;
import org.edu.util.StudentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private AcademicYearRepository academicYearRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private HouseRepository houseRepository;

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private ParentStudentRepository parentStudentRepository;

    @Mock
    private StudentEnrollmentRepository studentEnrollmentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    @Test
    void shouldDefaultStatusWhenCreatingStudentWithoutStatus() {
        StudentDTO request = new StudentDTO();
        request.setName("Sahan Perera");
        request.setDateOfBirth(LocalDate.of(2010, 3, 4));
        populateAdmissionDetails(request);
        Student mapped = new Student();
        mapped.setName("Sahan Perera");
        mapped.setDateOfBirth(LocalDate.of(2010, 3, 4));
        mapped.setNameWithInitials("S. Perera");
        mapped.setGender("Male");
        mapped.setAdmissionDate(LocalDate.of(2016, 1, 10));
        mapped.setHomeAddress("Maharagama");
        mapped.setGuardianRelationship("Father");
        mapped.setStatus(null);

        when(studentMapper.toEntity(request)).thenReturn(mapped);
        when(studentRepository.save(mapped)).thenReturn(mapped);
        when(parentStudentRepository.findByStudentId(null)).thenReturn(List.of());
        when(studentMapper.toDTO(mapped)).thenReturn(new StudentDTO());
        when(studentEnrollmentRepository.findByStudentIdAndStatusOrderByAcademicYearStartDateDesc(
                null,
                EnrollmentStatus.ACTIVE
        )).thenReturn(List.of());

        studentService.createStudent(request);

        assertEquals(StudentStatus.ACTIVE, mapped.getStatus());
    }

    @Test
    void shouldUseGuardianRelationshipWhenLinkingExistingParent() {
        StudentDTO request = new StudentDTO();
        request.setName("Sahan Perera");
        request.setDateOfBirth(LocalDate.of(2010, 3, 4));
        request.setParentIds(List.of(5L));
        populateAdmissionDetails(request);
        request.setGuardianRelationship("Mother");

        Student mapped = activeStudent(1L);
        mapped.setStatus(StudentStatus.ACTIVE);
        Parent parent = activeParent(5L, "Amali Perera", "0771234567");

        when(studentMapper.toEntity(request)).thenReturn(mapped);
        when(studentRepository.save(mapped)).thenReturn(mapped);
        when(parentStudentRepository.findByStudentId(1L)).thenReturn(List.of(), List.of());
        when(parentRepository.findByIdAndActiveTrue(5L)).thenReturn(Optional.of(parent));
        when(studentMapper.toDTO(mapped)).thenReturn(new StudentDTO());
        when(studentEnrollmentRepository.findByStudentIdAndStatusOrderByAcademicYearStartDateDesc(
                1L,
                EnrollmentStatus.ACTIVE
        )).thenReturn(List.of());

        studentService.createStudent(request);

        ArgumentCaptor<ParentStudent> captor = ArgumentCaptor.forClass(ParentStudent.class);
        verify(parentStudentRepository).save(captor.capture());
        assertEquals("Mother", captor.getValue().getRelationshipType());
    }

    @Test
    void shouldReuseExistingParentWhenInlineParentPhoneAlreadyExists() {
        StudentDTO request = new StudentDTO();
        request.setName("Sahan Perera");
        request.setDateOfBirth(LocalDate.of(2010, 3, 4));
        populateAdmissionDetails(request);
        request.setGuardianRelationship("Father");

        StudentParentInlineRequest inlineParent = new StudentParentInlineRequest();
        inlineParent.setName("Existing Father");
        inlineParent.setPhoneNumber("0771234567");
        inlineParent.setAddress("Maharagama");
        inlineParent.setOccupation("Engineer");
        request.setNewParents(List.of(inlineParent));

        Student mapped = activeStudent(1L);
        mapped.setStatus(StudentStatus.ACTIVE);
        Parent existingParent = activeParent(5L, "Existing Father", "0771234567");

        when(studentMapper.toEntity(request)).thenReturn(mapped);
        when(studentRepository.save(mapped)).thenReturn(mapped);
        when(parentStudentRepository.findByStudentId(1L)).thenReturn(List.of(), List.of());
        when(parentRepository.findByPhoneNumber("0771234567")).thenReturn(Optional.of(existingParent));
        when(parentStudentRepository.findByParentIdAndStudentId(5L, 1L)).thenReturn(Optional.empty());
        when(studentMapper.toDTO(mapped)).thenReturn(new StudentDTO());
        when(studentEnrollmentRepository.findByStudentIdAndStatusOrderByAcademicYearStartDateDesc(
                1L,
                EnrollmentStatus.ACTIVE
        )).thenReturn(List.of());

        studentService.createStudent(request);

        verify(parentRepository, never()).save(org.mockito.ArgumentMatchers.any(Parent.class));
        ArgumentCaptor<ParentStudent> captor = ArgumentCaptor.forClass(ParentStudent.class);
        verify(parentStudentRepository).save(captor.capture());
        assertEquals(existingParent, captor.getValue().getParent());
        assertEquals("Father", captor.getValue().getRelationshipType());
    }

    @Test
    void shouldRejectMissingGuardianRelationship() {
        StudentDTO request = new StudentDTO();
        request.setName("Sahan Perera");
        request.setDateOfBirth(LocalDate.of(2010, 3, 4));
        populateAdmissionDetails(request);
        request.setGuardianRelationship("");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> studentService.createStudent(request)
        );

        assertEquals("Guardian relationship is required", exception.getMessage());
        verify(studentRepository, never()).save(org.mockito.ArgumentMatchers.any(Student.class));
    }

    @Test
    void shouldRejectInvalidHouseWhenCreatingStudent() {
        StudentDTO request = new StudentDTO();
        request.setName("Sahan Perera");
        request.setDateOfBirth(LocalDate.of(2010, 3, 4));
        request.setHouseId(99L);
        populateAdmissionDetails(request);

        Student mapped = activeStudent(1L);
        mapped.setStatus(StudentStatus.ACTIVE);

        when(studentMapper.toEntity(request)).thenReturn(mapped);
        when(houseRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.createStudent(request));
        verify(studentRepository, never()).save(org.mockito.ArgumentMatchers.any(Student.class));
    }

    @Test
    void shouldSoftDeleteActiveStudent() {
        Student student = new Student();
        student.setId(1L);
        student.setActive(true);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        studentService.deleteStudent(1L);

        assertFalse(student.isActive());
        verify(studentRepository).save(student);
    }

    @Test
    void shouldFilterStudentsWithKeywordClassAndStatus() {
        Student student = activeStudent(1L);
        org.edu.entity.Class studentClass = activeClass(10L, "Grade 5A");
        student.setCurrentClass(studentClass);
        StudentDTO mapped = new StudentDTO();
        mapped.setId(1L);

        PageRequest pageable = PageRequest.of(0, 10);
        when(studentRepository.filterStudents("kamal", 10L, true, pageable))
                .thenReturn(new PageImpl<>(List.of(student), pageable, 1));
        when(studentMapper.toDTO(student)).thenReturn(mapped);
        when(parentStudentRepository.findByStudentId(1L)).thenReturn(List.of());
        when(studentEnrollmentRepository.findByStudentIdAndStatusOrderByAcademicYearStartDateDesc(
                1L,
                EnrollmentStatus.ACTIVE
        )).thenReturn(List.of());

        var page = studentService.filterStudents(" kamal ", 10L, true, pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals(1L, page.getContent().get(0).getId());
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
    void shouldRejectInitialClassOutsideCurrentAcademicYear() {
        StudentDTO request = new StudentDTO();
        request.setName("Sahan Perera");
        request.setDateOfBirth(LocalDate.of(2010, 3, 4));
        request.setCurrentClassId(10L);
        populateAdmissionDetails(request);

        Student mapped = new Student();
        mapped.setName("Sahan Perera");
        mapped.setDateOfBirth(LocalDate.of(2010, 3, 4));
        mapped.setStatus(StudentStatus.ACTIVE);

        AcademicYear currentYear = currentAcademicYear();
        AcademicYear oldYear = new AcademicYear();
        oldYear.setId(99L);
        org.edu.entity.Class oldClass = activeClass(10L, "Grade 1A");
        oldClass.setAcademicYear(oldYear);

        when(studentMapper.toEntity(request)).thenReturn(mapped);
        when(classRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(oldClass));
        when(academicYearRepository.findByCurrentTrueAndActiveTrue()).thenReturn(Optional.of(currentYear));

        assertThrows(IllegalArgumentException.class, () -> studentService.createStudent(request));
        verify(studentRepository, never()).save(org.mockito.ArgumentMatchers.any(Student.class));
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
        student.setHouse("Blue");
        student.setActive(true);
        return student;
    }

    private void populateAdmissionDetails(StudentDTO request) {
        request.setNameWithInitials("S. Perera");
        request.setGender("Male");
        request.setAdmissionDate(LocalDate.of(2016, 1, 10));
        request.setMedium("SINHALA");
        request.setHomeAddress("Maharagama");
        request.setGuardianRelationship("Father");
        request.setMedicalConditions("None");
    }

    private Parent activeParent(Long id, String name, String phoneNumber) {
        Parent parent = new Parent();
        parent.setId(id);
        parent.setName(name);
        parent.setPhoneNumber(phoneNumber);
        parent.setAddress("Maharagama");
        parent.setOccupation("Guardian");
        parent.setActive(true);
        return parent;
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
