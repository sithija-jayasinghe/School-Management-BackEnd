package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.ParentDTO;
import org.edu.entity.Parent;
import org.edu.entity.User;
import org.edu.exception.ResourceNotFoundException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    public ParentDTO createParent(ParentDTO parentDTO) {
        User user = userRepository.findById(parentDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (parentRepository.existsByUser(user)) {
            throw new IllegalArgumentException("User already assigned to a parent");
        }

        if (user.getRole() != Role.PARENT) {
            throw new IllegalArgumentException("User must have PARENT role");
        }

        Parent parent = parentMapper.toEntity(parentDTO);
        parent.setUser(user);
        parent.setActive(true);

        Parent savedParent = parentRepository.save(parent);

        if (parentDTO.getStudentIds() != null && !parentDTO.getStudentIds().isEmpty()) {
            for (Long studentId : parentDTO.getStudentIds()) {
                Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
                ParentStudent ps = new ParentStudent();
                ps.setParent(savedParent);
                ps.setStudent(student);
                ps.setRelationshipType("Parent"); // Default
                ps.setPrimaryContact(true); // Default
                parentStudentRepository.save(ps);
            }
        }

        return parentMapper.toDTO(savedParent);
    }

    @Override
    public ParentDTO updateParent(Long id, ParentDTO parentDTO) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));

        parentMapper.updateEntityFromDTO(parentDTO, parent);

        return parentMapper.toDTO(parent);
    }

    @Override
    public void deleteParent(Long id) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));

        parentRepository.delete(parent);
    }

    @Override
    public Page<ParentDTO> getAllParents(Pageable pageable) {
        return parentRepository.findAll(pageable)
                .map(parentMapper::toDTO);
    }

    @Override
    public ParentDTO getParentById(Long id) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));

        return parentMapper.toDTO(parent);
    }

    @Override
    public Page<ParentDTO> searchParents(String keyword, Pageable pageable) {
        return parentRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword, pageable)
                .map(parentMapper::toDTO);
    }

    @Override
    public List<ParentDTO> getAllActiveParents() {
        return parentRepository.findByActiveTrue()
                .stream()
                .map(parentMapper::toDTO)
                .toList();
    }

    @Override
    public List<ParentDTO> getAllInactiveParents() {
        return parentRepository.findByActiveFalse()
                .stream()
                .map(parentMapper::toDTO)
                .toList();
    }

    @Override
    public ParentDTO getParentByUserId(Long userId) {
        Parent parent = parentRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with user id: " + userId));

        return parentMapper.toDTO(parent);
    }

    @Override
    public void activateParent(Long id) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));

        if (parent.isActive()) {
            throw new IllegalStateException("Parent already active");
        }

        parent.setActive(true);
    }

    @Override
    public void deactivateParent(Long id) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));

        if (!parent.isActive()) {
            throw new IllegalStateException("Parent already inactive");
        }

        parent.setActive(false);
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
}
