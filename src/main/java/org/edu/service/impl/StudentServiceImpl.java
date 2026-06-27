package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.StudentDTO;
import org.edu.dto.StudentParentInlineRequest;
import org.edu.entity.Student;
import org.edu.entity.User;
import org.edu.entity.Class;
import org.edu.exception.InvalidAgeException;
import org.edu.exception.InvalidStudentDataException;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.StudentMapper;
import org.edu.repository.StudentRepository;
import org.edu.repository.UserRepository;
import org.edu.repository.ClassRepository;
import org.edu.service.StudentService;
import org.edu.util.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.edu.repository.ParentRepository;
import org.edu.repository.ParentStudentRepository;
import org.edu.entity.Parent;
import org.edu.entity.ParentStudent;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public StudentDTO createStudent(StudentDTO studentDTO) {

        User user = studentDTO.getUserId() == null
                ? createStudentUser(studentDTO)
                : resolveExistingStudentUser(studentDTO.getUserId());

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
        } else {
            student.setCurrentClass(null);
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

    private User createStudentUser(StudentDTO studentDTO) {
        User user = new User();
        user.setName(studentDTO.getName().trim());
        user.setEmail(generateLocalEmail(studentDTO.getName(), "student"));
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(Role.STUDENT);
        user.setActive(true);
        return userRepository.save(user);
    }

    private User createParentUser(StudentParentInlineRequest parentRequest) {
        User user = new User();
        user.setName(parentRequest.getName().trim());
        user.setEmail(generateLocalEmail(parentRequest.getName(), "parent"));
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(Role.PARENT);
        user.setActive(true);
        return userRepository.save(user);
    }

    private String generateLocalEmail(String name, String accountType) {
        String slug = name == null ? accountType : name.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", ".")
                .replaceAll("^\\.|\\.$", "");

        if (slug.isBlank()) {
            slug = accountType;
        }

        String email;
        do {
            email = slug + "." + UUID.randomUUID().toString().substring(0, 8) + "@" + accountType + ".school.local";
        } while (userRepository.existsByEmail(email));

        return email;
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
            parent.setUser(createParentUser(parentRequest));
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
        return dto;
    }
}
