package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.StudentDTO;
import org.edu.dto.StudentEnrollmentDTO;
import org.edu.dto.StudentParentInlineRequest;
import org.edu.entity.AcademicYear;
import org.edu.entity.Student;
import org.edu.entity.StudentEnrollment;
import org.edu.entity.User;
import org.edu.entity.Class;
import org.edu.exception.InvalidAgeException;
import org.edu.exception.InvalidStudentDataException;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.StudentMapper;
import org.edu.repository.StudentRepository;
import org.edu.repository.UserRepository;
import org.edu.repository.AcademicYearRepository;
import org.edu.repository.ClassRepository;
import org.edu.service.StudentService;
import org.edu.util.EnrollmentStatus;
import org.edu.util.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.edu.repository.ParentRepository;
import org.edu.repository.ParentStudentRepository;
import org.edu.repository.StudentEnrollmentRepository;
import org.edu.entity.Parent;
import org.edu.entity.ParentStudent;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final UserRepository userRepository;
    private final AcademicYearRepository academicYearRepository;
    private final ClassRepository classRepository;
    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;

    @Override
    public StudentDTO createStudent(StudentDTO studentDTO) {

        User user = studentDTO.getUserId() == null ? null : resolveExistingStudentUser(studentDTO.getUserId());

        if (studentDTO.getDateOfBirth().isAfter(LocalDate.now().minusYears(3))) {
            throw new InvalidAgeException("Invalid student age: Must be at least 3 years old");
        }

        Student student = studentMapper.toEntity(studentDTO);
        student.setUser(user);
        student.setActive(true);

        if (studentDTO.getCurrentClassId() != null) {
            Class clazz = classRepository.findByIdAndActiveTrue(studentDTO.getCurrentClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + studentDTO.getCurrentClassId()));
            student.setCurrentClass(clazz);
        }

        Student savedStudent = studentRepository.save(student);

        syncParentLinks(savedStudent, studentDTO.getParentIds());
        createAndLinkNewParents(savedStudent, studentDTO.getNewParents());
        syncCurrentEnrollment(savedStudent);

        return toDTOWithParentIds(savedStudent);
    }

    @Override
    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {

        Student student = studentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (studentDTO.getDateOfBirth() != null) {
            if (studentDTO.getDateOfBirth().isAfter(LocalDate.now().minusYears(3))) {
                throw new InvalidAgeException("Invalid student age: Must be at least 3 years old");
            }
        }

        studentMapper.updateEntityFromDTO(studentDTO, student);

        if (studentDTO.getCurrentClassId() != null) {
            Class clazz = classRepository.findByIdAndActiveTrue(studentDTO.getCurrentClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + studentDTO.getCurrentClassId()));
            student.setCurrentClass(clazz);
            syncCurrentEnrollment(student);
        } else {
            student.setCurrentClass(null);
            closeCurrentEnrollment(student);
        }

        if (studentDTO.getParentIds() != null) {
            syncParentLinks(student, studentDTO.getParentIds());
        }

        createAndLinkNewParents(student, studentDTO.getNewParents());

        return toDTOWithParentIds(student);
    }

    @Override
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (!student.isActive()) {
            throw new IllegalStateException("Student already inactive");
        }

        student.setActive(false);
    }

    @Override
    public Page<StudentDTO> getAllStudents(Pageable pageable) {

        return studentRepository.findByActiveTrue(pageable)
                .map(this::toDTOWithParentIds);
    }

    @Override
    public StudentDTO getStudentById(Long id) {

        Student student = studentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        return toDTOWithParentIds(student);
    }

    @Override
    public Page<StudentDTO> searchStudents(String name, Pageable pageable) {

        return studentRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                .map(this::toDTOWithParentIds);
    }

    @Override
    public List<StudentDTO> getAllActiveStudents() {

        return studentRepository.findByActiveTrue()
                .stream()
                .map(this::toDTOWithParentIds)
                .toList();
    }

    @Override
    public List<StudentDTO> getStudentsByClassId(Long classId) {
        Class clazz = classRepository.findByIdAndActiveTrue(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        return clazz.getStudents().stream()
                .filter(Student::isActive)
                .map(this::toDTOWithParentIds)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentEnrollmentDTO> getStudentEnrollments(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }

        return studentEnrollmentRepository.findByStudentIdOrderByAcademicYearStartDateDesc(studentId)
                .stream()
                .map(this::toEnrollmentDTO)
                .toList();
    }

    private User resolveExistingStudentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (studentRepository.existsByUser(user)) {
            throw new InvalidStudentDataException("User already assigned to a student");
        }

        if (user.getRole() != Role.STUDENT) {
            throw new InvalidStudentDataException("User must have STUDENT role");
        }

        return user;
    }

    private void syncParentLinks(Student student, List<Long> parentIds) {
        Set<Long> requestedParentIds = new LinkedHashSet<>(parentIds == null ? List.of() : parentIds);
        List<ParentStudent> existingLinks = parentStudentRepository.findByStudentId(student.getId());

        existingLinks.stream()
                .filter(link -> !requestedParentIds.contains(link.getParent().getId()))
                .forEach(parentStudentRepository::delete);

        Set<Long> existingParentIds = existingLinks.stream()
                .map(link -> link.getParent().getId())
                .collect(java.util.stream.Collectors.toSet());

        requestedParentIds.stream()
                .filter(parentId -> !existingParentIds.contains(parentId))
                .forEach(parentId -> {
                    Parent parent = parentRepository.findByIdAndActiveTrue(parentId)
                            .orElseThrow(() -> new ResourceNotFoundException("Active parent not found with id: " + parentId));
                    ParentStudent parentStudent = new ParentStudent();
                    parentStudent.setParent(parent);
                    parentStudent.setStudent(student);
                    parentStudent.setRelationshipType("Parent");
                    parentStudent.setPrimaryContact(true);
                    parentStudent.setEmergencyContact(false);
                    parentStudentRepository.save(parentStudent);
                });
    }

    private void createAndLinkNewParents(Student student, List<StudentParentInlineRequest> newParents) {
        if (newParents == null || newParents.isEmpty()) {
            return;
        }

        for (StudentParentInlineRequest parentRequest : newParents) {
            Parent parent = new Parent();
            parent.setName(parentRequest.getName().trim());
            parent.setPhoneNumber(parentRequest.getPhoneNumber().trim());
            parent.setAddress(parentRequest.getAddress().trim());
            parent.setOccupation(parentRequest.getOccupation().trim());
            parent.setActive(true);
            Parent savedParent = parentRepository.save(parent);

            ParentStudent parentStudent = new ParentStudent();
            parentStudent.setParent(savedParent);
            parentStudent.setStudent(student);
            parentStudent.setRelationshipType("Parent");
            parentStudent.setPrimaryContact(true);
            parentStudent.setEmergencyContact(false);
            parentStudentRepository.save(parentStudent);
        }
    }

    private StudentDTO toDTOWithParentIds(Student student) {
        StudentDTO dto = studentMapper.toDTO(student);
        dto.setParentIds(parentStudentRepository.findByStudentId(student.getId()).stream()
                .map(link -> link.getParent().getId())
                .toList());
        studentEnrollmentRepository
                .findByStudentIdAndStatusOrderByAcademicYearStartDateDesc(student.getId(), EnrollmentStatus.ACTIVE)
                .stream()
                .findFirst()
                .ifPresent(enrollment -> {
                    dto.setCurrentAcademicYearId(enrollment.getAcademicYear().getId());
                    dto.setCurrentAcademicYearName(enrollment.getAcademicYear().getName());
                    dto.setCurrentClassId(enrollment.getStudentClass().getId());
                    dto.setCurrentClassName(enrollment.getStudentClass().getName());
                });
        return dto;
    }

    private StudentEnrollmentDTO toEnrollmentDTO(StudentEnrollment enrollment) {
        StudentEnrollmentDTO dto = new StudentEnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setStudentId(enrollment.getStudent().getId());
        dto.setStudentName(enrollment.getStudent().getName());
        dto.setAcademicYearId(enrollment.getAcademicYear().getId());
        dto.setAcademicYearName(enrollment.getAcademicYear().getName());
        dto.setClassId(enrollment.getStudentClass().getId());
        dto.setClassName(enrollment.getStudentClass().getName());
        dto.setStartDate(enrollment.getStartDate());
        dto.setEndDate(enrollment.getEndDate());
        dto.setStatus(enrollment.getStatus());
        dto.setCreatedAt(enrollment.getCreatedAt());
        dto.setUpdatedAt(enrollment.getUpdatedAt());
        return dto;
    }

    private void syncCurrentEnrollment(Student student) {
        if (student.getCurrentClass() == null) {
            return;
        }

        AcademicYear academicYear = academicYearRepository.findByCurrentTrueAndActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Current academic year not found"));

        StudentEnrollment enrollment = studentEnrollmentRepository
                .findByStudentIdAndAcademicYearIdAndStatus(
                        student.getId(),
                        academicYear.getId(),
                        EnrollmentStatus.ACTIVE
                )
                .map(activeEnrollment -> {
                    if (activeEnrollment.getStudentClass().getId().equals(student.getCurrentClass().getId())) {
                        return activeEnrollment;
                    }

                    activeEnrollment.setStatus(EnrollmentStatus.TRANSFERRED);
                    activeEnrollment.setEndDate(LocalDate.now());
                    return createEnrollment(student, academicYear);
                })
                .orElseGet(() -> createEnrollment(student, academicYear));

        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEndDate(null);
        studentEnrollmentRepository.save(enrollment);
    }

    private void closeCurrentEnrollment(Student student) {
        academicYearRepository.findByCurrentTrueAndActiveTrue()
                .flatMap(academicYear -> studentEnrollmentRepository.findByStudentIdAndAcademicYearIdAndStatus(
                        student.getId(),
                        academicYear.getId(),
                        EnrollmentStatus.ACTIVE
                ))
                .ifPresent(enrollment -> {
                    enrollment.setStatus(EnrollmentStatus.TRANSFERRED);
                    enrollment.setEndDate(LocalDate.now());
                });
    }

    private StudentEnrollment createEnrollment(Student student, AcademicYear academicYear) {
        StudentEnrollment enrollment = new StudentEnrollment();
        enrollment.setStudent(student);
        enrollment.setAcademicYear(academicYear);
        enrollment.setStudentClass(student.getCurrentClass());
        enrollment.setStartDate(LocalDate.now());
        return enrollment;
    }
}
