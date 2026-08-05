package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.StudentDTO;
import org.edu.dto.StudentEnrollmentDTO;
import org.edu.dto.StudentParentInlineRequest;
import org.edu.entity.AcademicYear;
import org.edu.entity.Activity;
import org.edu.entity.Student;
import org.edu.entity.StudentEnrollment;
import org.edu.entity.Class;
import org.edu.entity.User;
import org.edu.exception.InvalidAgeException;
import org.edu.exception.ResourceNotFoundException;
import org.edu.filter.FilterSpecifications;
import org.edu.filter.StudentFilterDefinitions;
import org.edu.mapper.StudentMapper;
import org.edu.repository.StudentRepository;
import org.edu.repository.AcademicYearRepository;
import org.edu.repository.ActivityRepository;
import org.edu.repository.ClassRepository;
import org.edu.repository.HouseRepository;
import org.edu.service.StudentService;
import org.edu.util.EnrollmentStatus;
import org.edu.util.Role;
import org.edu.util.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.edu.repository.ParentRepository;
import org.edu.repository.ParentStudentRepository;
import org.edu.repository.StudentEnrollmentRepository;
import org.edu.repository.UserRepository;
import org.edu.entity.Parent;
import org.edu.entity.ParentStudent;
import org.edu.entity.House;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final AcademicYearRepository academicYearRepository;
    private final ActivityRepository activityRepository;
    private final ClassRepository classRepository;
    private final HouseRepository houseRepository;
    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public StudentDTO createStudent(StudentDTO studentDTO) {

        validateAdmissionDetails(studentDTO, null);

        if (studentDTO.getDateOfBirth().isAfter(LocalDate.now().minusYears(3))) {
            throw new InvalidAgeException("Invalid student age: Must be at least 3 years old");
        }

        Student student = studentMapper.toEntity(studentDTO);
        student.setActive(true);
        normalizeAdmissionDetails(student);
        applyStudentDefaults(student);
        applyHouse(student, studentDTO);
        syncStudentActivities(student, studentDTO.getActivityIds());

        if (studentDTO.getCurrentClassId() != null) {
            Class clazz = classRepository.findByIdAndActiveTrue(studentDTO.getCurrentClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + studentDTO.getCurrentClassId()));
            validateInitialClassForCurrentAcademicYear(clazz);
            student.setCurrentClass(clazz);
        }

        Student savedStudent = studentRepository.save(student);

        syncParentLinks(savedStudent, studentDTO.getParentIds(), studentDTO.getGuardianRelationship());
        createAndLinkNewParents(savedStudent, studentDTO.getNewParents(), studentDTO.getGuardianRelationship());
        syncCurrentEnrollment(savedStudent);

        return toDTOWithParentIds(savedStudent);
    }

    @Override
    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {

        Student student = studentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        validateAdmissionDetails(studentDTO, id);

        if (studentDTO.getDateOfBirth() != null) {
            if (studentDTO.getDateOfBirth().isAfter(LocalDate.now().minusYears(3))) {
                throw new InvalidAgeException("Invalid student age: Must be at least 3 years old");
            }
        }

        studentMapper.updateEntityFromDTO(studentDTO, student);
        normalizeAdmissionDetails(student);
        applyStudentDefaults(student);
        applyHouse(student, studentDTO);
        syncStudentActivities(student, studentDTO.getActivityIds());

        if (studentDTO.getParentIds() != null) {
            syncParentLinks(student, studentDTO.getParentIds(), studentDTO.getGuardianRelationship());
        }

        createAndLinkNewParents(student, studentDTO.getNewParents(), studentDTO.getGuardianRelationship());

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
        studentRepository.save(student);
    }

    @Override
    public Page<StudentDTO> getAllStudents(Pageable pageable) {

        return studentRepository.findByActiveTrue(pageable)
                .map(this::toDTOWithParentIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentDTO> filterStudents(Map<String, String> filters, Pageable pageable) {
        return studentRepository.findAll(
                        FilterSpecifications.build(filters, StudentFilterDefinitions.definitions(houseRepository)),
                        pageable
                )
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

    @Override
    public StudentDTO transferStudent(Long studentId, Long classId) {
        return changeStudentEnrollment(studentId, classId, EnrollmentStatus.TRANSFERRED);
    }

    @Override
    public StudentDTO promoteStudent(Long studentId, Long classId) {
        return changeStudentEnrollment(studentId, classId, EnrollmentStatus.PROMOTED);
    }

    @Override
    public StudentDTO withdrawStudent(Long studentId) {
        return closeStudentEnrollment(studentId, EnrollmentStatus.WITHDRAWN);
    }

    @Override
    public StudentDTO completeStudent(Long studentId) {
        return closeStudentEnrollment(studentId, EnrollmentStatus.COMPLETED);
    }

    private void syncParentLinks(Student student, List<Long> parentIds, String relationshipType) {
        Set<Long> requestedParentIds = new LinkedHashSet<>(parentIds == null ? List.of() : parentIds);
        List<ParentStudent> existingLinks = parentStudentRepository.findByStudentId(student.getId());
        String normalizedRelationship = normalizeRelationshipType(relationshipType);

        existingLinks.stream()
                .filter(link -> !requestedParentIds.contains(link.getParent().getId()))
                .forEach(parentStudentRepository::delete);

        Set<Long> existingParentIds = existingLinks.stream()
                .map(link -> link.getParent().getId())
                .collect(java.util.stream.Collectors.toSet());

        existingLinks.stream()
                .filter(link -> requestedParentIds.contains(link.getParent().getId()))
                .forEach(link -> {
                    link.setRelationshipType(normalizedRelationship);
                    link.setPrimaryContact(true);
                    link.setEmergencyContact(false);
                });

        requestedParentIds.stream()
                .filter(parentId -> !existingParentIds.contains(parentId))
                .forEach(parentId -> {
                    Parent parent = parentRepository.findByIdAndActiveTrue(parentId)
                            .orElseThrow(() -> new ResourceNotFoundException("Active parent not found with id: " + parentId));
                    ParentStudent parentStudent = new ParentStudent();
                    parentStudent.setParent(parent);
                    parentStudent.setStudent(student);
                    parentStudent.setRelationshipType(normalizedRelationship);
                    parentStudent.setPrimaryContact(true);
                    parentStudent.setEmergencyContact(false);
                    parentStudentRepository.save(parentStudent);
                });
    }

    private void applyStudentDefaults(Student student) {
        if (student.getStatus() == null) {
            student.setStatus(StudentStatus.ACTIVE);
        }
    }

    private void validateAdmissionDetails(StudentDTO studentDTO, Long currentStudentId) {
        requireText(studentDTO.getNameWithInitials(), "Name with initials is required");
        requireText(studentDTO.getGender(), "Gender is required");
        requireText(studentDTO.getHomeAddress(), "Home address is required");
        requireText(studentDTO.getGuardianRelationship(), "Guardian relationship is required");

        if (studentDTO.getAdmissionDate() == null) {
            throw new IllegalArgumentException("Admission date is required");
        }

        if (studentDTO.getMedium() == null || studentDTO.getMedium().isBlank()) {
            throw new IllegalArgumentException("Medium is required");
        }

        if (studentDTO.getDateOfBirth() != null && studentDTO.getAdmissionDate().isBefore(studentDTO.getDateOfBirth())) {
            throw new IllegalArgumentException("Admission date cannot be before date of birth");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void normalizeAdmissionDetails(Student student) {
        student.setAdmissionNumber(trimToNull(student.getAdmissionNumber()));
        student.setName(trimToNull(student.getName()));
        student.setNameWithInitials(trimToNull(student.getNameWithInitials()));
        student.setGender(trimToNull(student.getGender()));
        student.setHomeAddress(trimToNull(student.getHomeAddress()));
        student.setGuardianRelationship(trimToNull(student.getGuardianRelationship()));
        student.setMedicalConditions(trimToNull(student.getMedicalConditions()));
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private void applyHouse(Student student, StudentDTO studentDTO) {
        if (studentDTO.getHouseId() == null) {
            return;
        }

        House house = houseRepository.findByIdAndActiveTrue(studentDTO.getHouseId())
                .orElseThrow(() -> new ResourceNotFoundException("House not found with id: " + studentDTO.getHouseId()));
        student.setAssignedHouse(house);
        student.setHouse(house.getName());
    }

    private void createAndLinkNewParents(Student student, List<StudentParentInlineRequest> newParents, String relationshipType) {
        if (newParents == null || newParents.isEmpty()) {
            return;
        }

        String normalizedRelationship = normalizeRelationshipType(relationshipType);
        for (StudentParentInlineRequest parentRequest : newParents) {
            Parent parent = findOrCreateParent(parentRequest);
            linkParentToStudent(parent, student, normalizedRelationship);
        }
    }

    private Parent findOrCreateParent(StudentParentInlineRequest parentRequest) {
        String phoneNumber = parentRequest.getPhoneNumber().trim();
        // DEMO-FEATURE: parent-nic-class-status-filters START
        String nic = normalizeParentNic(parentRequest.getNic());
        // DEMO-FEATURE: parent-nic-class-status-filters END
        return parentRepository.findByPhoneNumber(phoneNumber)
                .map(parent -> {
                    parent.setActive(true);
                    // DEMO-FEATURE: parent-nic-class-status-filters START
                    if (nic != null && (parent.getNic() == null || parent.getNic().isBlank())) {
                        parent.setNic(nic);
                    }
                    // DEMO-FEATURE: parent-nic-class-status-filters END
                    ensureParentUser(parent, parentRequest);
                    return parent;
                })
                .orElseGet(() -> {
                    Parent parent = new Parent();
                    parent.setName(parentRequest.getName().trim());
                    parent.setPhoneNumber(phoneNumber);
                    // DEMO-FEATURE: parent-nic-class-status-filters START
                    parent.setNic(nic);
                    // DEMO-FEATURE: parent-nic-class-status-filters END
                    parent.setAddress(parentRequest.getAddress().trim());
                    parent.setOccupation(parentRequest.getOccupation().trim());
                    parent.setUser(createParentUser(parentRequest));
                    parent.setActive(true);
                    return parentRepository.save(parent);
                });
    }

    // DEMO-FEATURE: parent-nic-class-status-filters START
    private String normalizeParentNic(String nic) {
        if (nic == null || nic.isBlank()) {
            return null;
        }

        return nic.trim().toUpperCase();
    }
    // DEMO-FEATURE: parent-nic-class-status-filters END

    private void ensureParentUser(Parent parent, StudentParentInlineRequest parentRequest) {
        if (parent.getUser() != null) {
            return;
        }

        parent.setUser(createParentUser(parentRequest));
    }

    private User createParentUser(StudentParentInlineRequest parentRequest) {
        String email = requiredTrim(parentRequest.getEmail(), "Parent email is required").toLowerCase();
        String password = requiredTrim(parentRequest.getPassword(), "Parent password is required");

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Parent email is already registered");
        }

        User user = new User();
        user.setName(parentRequest.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.PARENT);
        user.setActive(true);
        return userRepository.save(user);
    }

    private String requiredTrim(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }

    private void linkParentToStudent(Parent parent, Student student, String relationshipType) {
        parentStudentRepository.findByParentIdAndStudentId(parent.getId(), student.getId())
                .ifPresentOrElse(existingLink -> {
                    existingLink.setRelationshipType(relationshipType);
                    existingLink.setPrimaryContact(true);
                    existingLink.setEmergencyContact(false);
                }, () -> {
                    ParentStudent parentStudent = new ParentStudent();
                    parentStudent.setParent(parent);
                    parentStudent.setStudent(student);
                    parentStudent.setRelationshipType(relationshipType);
                    parentStudent.setPrimaryContact(true);
                    parentStudent.setEmergencyContact(false);
                    parentStudentRepository.save(parentStudent);
                });
    }

    private String normalizeRelationshipType(String relationshipType) {
        return relationshipType == null || relationshipType.isBlank()
                ? "Guardian"
                : relationshipType.trim();
    }

    private StudentDTO toDTOWithParentIds(Student student) {
        StudentDTO dto = studentMapper.toDTO(student);
        dto.setActivityIds(student.getActivities().stream()
                .map(Activity::getId)
                .toList());
        dto.setActivityNames(student.getActivities().stream()
                .map(Activity::getName)
                .toList());
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

    private void syncStudentActivities(Student student, List<Long> activityIds) {
        if (activityIds == null) {
            student.getActivities().clear();
            return;
        }

        LinkedHashSet<Activity> activities = activityIds.stream()
                .distinct()
                .map(activityId -> activityRepository.findByIdAndActiveTrue(activityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId)))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        student.getActivities().clear();
        student.getActivities().addAll(activities);
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

        AcademicYear academicYear = getCurrentAcademicYear();
        List<StudentEnrollment> activeEnrollments = findActiveEnrollments(student, academicYear);
        StudentEnrollment enrollment = activeEnrollments.stream()
                .filter(activeEnrollment -> activeEnrollment.getStudentClass().getId().equals(student.getCurrentClass().getId()))
                .findFirst()
                .orElseGet(() -> {
                    activeEnrollments.forEach(activeEnrollment -> closeEnrollment(activeEnrollment, EnrollmentStatus.TRANSFERRED));
                    return createEnrollment(student, academicYear);
                });

        activeEnrollments.stream()
                .filter(activeEnrollment -> !activeEnrollment.getId().equals(enrollment.getId()))
                .forEach(activeEnrollment -> closeEnrollment(activeEnrollment, EnrollmentStatus.TRANSFERRED));

        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEndDate(null);
        studentEnrollmentRepository.save(enrollment);
    }

    private StudentDTO changeStudentEnrollment(Long studentId, Long classId, EnrollmentStatus closedStatus) {
        Student student = getActiveStudent(studentId);
        Class clazz = classRepository.findByIdAndActiveTrue(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));
        AcademicYear academicYear = getCurrentAcademicYear();
        List<StudentEnrollment> activeEnrollments = findActiveEnrollments(student, academicYear);

        if (activeEnrollments.stream().anyMatch(activeEnrollment -> activeEnrollment.getStudentClass().getId().equals(clazz.getId()))) {
            throw new IllegalStateException("Student is already enrolled in this class for the current academic year");
        }

        activeEnrollments.forEach(activeEnrollment -> closeEnrollment(activeEnrollment, closedStatus));

        student.setCurrentClass(clazz);
        StudentEnrollment enrollment = createEnrollment(student, academicYear);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        studentEnrollmentRepository.save(enrollment);
        studentRepository.save(student);

        return toDTOWithParentIds(student);
    }

    private StudentDTO closeStudentEnrollment(Long studentId, EnrollmentStatus status) {
        Student student = getActiveStudent(studentId);
        AcademicYear academicYear = getCurrentAcademicYear();
        List<StudentEnrollment> activeEnrollments = findActiveEnrollments(student, academicYear);

        if (activeEnrollments.isEmpty()) {
            throw new ResourceNotFoundException("Active enrollment not found for current academic year");
        }

        activeEnrollments.forEach(enrollment -> closeEnrollment(enrollment, status));
        student.setCurrentClass(null);
        studentRepository.save(student);

        return toDTOWithParentIds(student);
    }

    private Student getActiveStudent(Long studentId) {
        return studentRepository.findByIdAndActiveTrue(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
    }

    private AcademicYear getCurrentAcademicYear() {
        return academicYearRepository.findByCurrentTrueAndActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Current academic year not found"));
    }

    private void validateInitialClassForCurrentAcademicYear(Class clazz) {
        AcademicYear currentAcademicYear = getCurrentAcademicYear();
        if (clazz.getAcademicYear() == null || !currentAcademicYear.getId().equals(clazz.getAcademicYear().getId())) {
            throw new IllegalArgumentException("Initial class must belong to the current academic year");
        }
    }

    private void closeCurrentEnrollment(Student student) {
        academicYearRepository.findByCurrentTrueAndActiveTrue()
                .ifPresent(academicYear -> findActiveEnrollments(student, academicYear)
                        .forEach(enrollment -> closeEnrollment(enrollment, EnrollmentStatus.TRANSFERRED)));
    }

    private List<StudentEnrollment> findActiveEnrollments(Student student, AcademicYear academicYear) {
        return studentEnrollmentRepository.findByStudentIdAndAcademicYearIdAndStatusOrderByStartDateDesc(
                student.getId(),
                academicYear.getId(),
                EnrollmentStatus.ACTIVE
        );
    }

    private void closeEnrollment(StudentEnrollment enrollment, EnrollmentStatus status) {
        enrollment.setStatus(status);
        enrollment.setEndDate(LocalDate.now());
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
