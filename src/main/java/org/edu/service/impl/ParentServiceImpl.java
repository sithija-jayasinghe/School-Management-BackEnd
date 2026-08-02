package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.ParentDTO;
import org.edu.entity.Parent;
import org.edu.entity.User;
import org.edu.exception.DuplicateEmailException;
import org.edu.exception.ResourceNotFoundException;
import org.edu.filter.FilterSpecifications;
import org.edu.filter.ParentFilterDefinitions;
import org.edu.mapper.ParentMapper;
import org.edu.repository.ParentRepository;
import org.edu.repository.UserRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.ParentStudentRepository;
import org.edu.service.ParentService;
import org.edu.dto.ParentStudentDTO;
import org.edu.dto.request.ParentStudentRequest;
import org.edu.entity.ParentStudent;
import org.edu.entity.Student;
import org.edu.mapper.ParentStudentMapper;
import org.edu.util.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final ParentRepository parentRepository;
    private final ParentMapper parentMapper;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final ParentStudentMapper parentStudentMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ParentDTO createParent(ParentDTO parentDTO) {
        Parent parent = parentMapper.toEntity(parentDTO);
        User user = resolveUserForParent(parentDTO);
        if (user != null) {
            parent.setUser(user);
        }
        parent.setActive(true);

        return parentMapper.toDTO(parentRepository.save(parent));
    }

    @Override
    public ParentDTO updateParent(Long id, ParentDTO parentDTO) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));

        parentMapper.updateEntityFromDTO(parentDTO, parent);
        if (parentDTO.getUserId() != null && (parent.getUser() == null || !parent.getUser().getId().equals(parentDTO.getUserId()))) {
            parent.setUser(resolveAvailableParentUser(parentDTO.getUserId()));
        }

        return parentMapper.toDTO(parent);
    }

    @Override
    public void deleteParent(Long id) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));

        if (!parent.isActive()) {
            throw new IllegalStateException("Parent already inactive");
        }

        parent.setActive(false);
        parentRepository.save(parent);
    }

    @Override
    public Page<ParentDTO> getAllParents(Pageable pageable) {
        return parentRepository.findAll(pageable)
                .map(this::toDTOWithStudentIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ParentDTO> filterParents(Map<String, String> filters, Pageable pageable) {
        return parentRepository.findAll(FilterSpecifications.build(filters, ParentFilterDefinitions.definitions()), pageable)
                .map(this::toDTOWithStudentIds);
    }

    @Override
    public ParentDTO getParentById(Long id) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));

        return toDTOWithStudentIds(parent);
    }

    @Override
    public Page<ParentDTO> searchParents(String keyword, Pageable pageable) {
        return parentRepository.searchActiveParents(keyword.trim(), pageable)
                .map(this::toDTOWithStudentIds);
    }

    @Override
    public List<ParentDTO> getAllActiveParents() {
        return parentRepository.findByActiveTrue()
                .stream()
                .map(this::toDTOWithStudentIds)
                .toList();
    }

    @Override
    public List<ParentDTO> getAllInactiveParents() {
        return parentRepository.findByActiveFalse()
                .stream()
                .map(this::toDTOWithStudentIds)
                .toList();
    }

    @Override
    public ParentDTO getParentByUserId(Long userId) {
        Parent parent = parentRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with user id: " + userId));

        return toDTOWithStudentIds(parent);
    }

    private ParentDTO toDTOWithStudentIds(Parent parent) {
        ParentDTO dto = parentMapper.toDTO(parent);
        dto.setStudentIds(parentStudentRepository.findByParentId(parent.getId()).stream()
                .map(link -> link.getStudent().getId())
                .toList());
        return dto;
    }

    @Override
    public void activateParent(Long id) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));

        if (parent.isActive()) {
            throw new IllegalStateException("Parent already active");
        }

        parent.setActive(true);
        parentRepository.save(parent);
    }

    @Override
    public void deactivateParent(Long id) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));

        if (!parent.isActive()) {
            throw new IllegalStateException("Parent already inactive");
        }

        parent.setActive(false);
        parentRepository.save(parent);
    }

    @Override
    public ParentStudentDTO linkParentToStudent(Long parentId, Long studentId, ParentStudentRequest request) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + parentId));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        if (parentStudentRepository.existsByParentIdAndStudentId(parentId, studentId)) {
            throw new IllegalArgumentException("Parent-Student relationship already exists");
        }

        ParentStudent parentStudent = new ParentStudent();
        parentStudent.setParent(parent);
        parentStudent.setStudent(student);
        parentStudent.setRelationshipType(request.getRelationshipType());
        parentStudent.setPrimaryContact(request.isPrimaryContact());
        parentStudent.setEmergencyContact(request.isEmergencyContact());

        return parentStudentMapper.toDTO(parentStudentRepository.save(parentStudent));
    }

    @Override
    public void unlinkParentFromStudent(Long parentId, Long studentId) {
        ParentStudent parentStudent = parentStudentRepository.findByParentIdAndStudentId(parentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Relationship not found between parent " + parentId + " and student " + studentId));

        parentStudentRepository.delete(parentStudent);
    }

    @Override
    public List<ParentStudentDTO> getStudentsByParent(Long parentId) {
        if (!parentRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Parent not found with id: " + parentId);
        }

        return parentStudentRepository.findByParentId(parentId).stream()
                .map(parentStudentMapper::toDTO)
                .toList();
    }

    @Override
    public List<ParentStudentDTO> getParentsByStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }

        return parentStudentRepository.findByStudentId(studentId).stream()
                .map(parentStudentMapper::toDTO)
                .toList();
    }

    @Override
    public ParentStudentDTO getRelationship(Long parentId, Long studentId) {
        ParentStudent parentStudent = parentStudentRepository.findByParentIdAndStudentId(parentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Relationship not found between parent " + parentId + " and student " + studentId));

        return parentStudentMapper.toDTO(parentStudent);
    }

    @Override
    public ParentStudentDTO updateRelationship(Long parentId, Long studentId, ParentStudentRequest request) {
        ParentStudent parentStudent = parentStudentRepository.findByParentIdAndStudentId(parentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Relationship not found between parent " + parentId + " and student " + studentId));

        parentStudent.setRelationshipType(request.getRelationshipType());
        parentStudent.setPrimaryContact(request.isPrimaryContact());
        parentStudent.setEmergencyContact(request.isEmergencyContact());

        return parentStudentMapper.toDTO(parentStudentRepository.save(parentStudent));
    }

    @Override
    public void setPrimaryContact(Long parentId, Long studentId) {
        ParentStudent parentStudent = parentStudentRepository.findByParentIdAndStudentId(parentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Relationship not found between parent " + parentId + " and student " + studentId));

        parentStudent.setPrimaryContact(true);
        parentStudentRepository.save(parentStudent);
    }

    @Override
    public void setEmergencyContact(Long parentId, Long studentId) {
        ParentStudent parentStudent = parentStudentRepository.findByParentIdAndStudentId(parentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Relationship not found between parent " + parentId + " and student " + studentId));

        parentStudent.setEmergencyContact(true);
        parentStudentRepository.save(parentStudent);
    }

    @Override
    public void removePrimaryContact(Long parentId, Long studentId) {
        ParentStudent parentStudent = parentStudentRepository.findByParentIdAndStudentId(parentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Relationship not found between parent " + parentId + " and student " + studentId));

        parentStudent.setPrimaryContact(false);
        parentStudentRepository.save(parentStudent);
    }

    @Override
    public void removeEmergencyContact(Long parentId, Long studentId) {
        ParentStudent parentStudent = parentStudentRepository.findByParentIdAndStudentId(parentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Relationship not found between parent " + parentId + " and student " + studentId));

        parentStudent.setEmergencyContact(false);
        parentStudentRepository.save(parentStudent);
    }

    private User resolveAvailableParentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (parentRepository.existsByUser(user)) {
            throw new IllegalArgumentException("User already assigned to a parent");
        }

        if (user.getRole() != Role.PARENT) {
            throw new IllegalArgumentException("User must have PARENT role");
        }

        return user;
    }

    private User resolveUserForParent(ParentDTO parentDTO) {
        boolean hasInlineLogin = hasText(parentDTO.getLoginEmail()) || hasText(parentDTO.getLoginPassword());
        if (parentDTO.getUserId() != null && hasInlineLogin) {
            throw new IllegalArgumentException("Provide either an existing userId or new login details, not both");
        }
        if (parentDTO.getUserId() != null) {
            return resolveAvailableParentUser(parentDTO.getUserId());
        }
        if (hasInlineLogin) {
            return createLinkedUser(parentDTO.getName(), parentDTO.getLoginEmail(), parentDTO.getLoginPassword());
        }
        return null;
    }

    private User createLinkedUser(String name, String email, String password) {
        if (!hasText(email) || !hasText(password)) {
            throw new IllegalArgumentException("Login email and password are both required to create a login");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("Email is already registered");
        }
        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.PARENT);
        user.setActive(true);
        return userRepository.save(user);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
