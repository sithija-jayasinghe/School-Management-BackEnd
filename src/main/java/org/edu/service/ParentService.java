package org.edu.service;

import org.edu.dto.ParentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ParentService {

    ParentDTO createParent(ParentDTO parentDTO);
    ParentDTO updateParent(Long id, ParentDTO parentDTO);
    void deleteParent(Long id); // Hard delete
    Page<ParentDTO> getAllParents(Pageable pageable);
    ParentDTO getParentById(Long id);
    Page<ParentDTO> searchParents(String keyword, Pageable pageable);
    List<ParentDTO> getAllActiveParents();
    List<ParentDTO> getAllInactiveParents();
    ParentDTO getParentByUserId(Long userId);
    void activateParent(Long id);
    void deactivateParent(Long id);

    // Parent-Student Link APIs
    org.edu.dto.ParentStudentDTO linkParentToStudent(Long parentId, Long studentId, org.edu.dto.request.ParentStudentRequest request);
    void unlinkParentFromStudent(Long parentId, Long studentId);
    List<org.edu.dto.ParentStudentDTO> getStudentsByParent(Long parentId);
    List<org.edu.dto.ParentStudentDTO> getParentsByStudent(Long studentId);
    org.edu.dto.ParentStudentDTO getRelationship(Long parentId, Long studentId);
    org.edu.dto.ParentStudentDTO updateRelationship(Long parentId, Long studentId, org.edu.dto.request.ParentStudentRequest request);
    void setPrimaryContact(Long parentId, Long studentId);
    void setEmergencyContact(Long parentId, Long studentId);
    void removePrimaryContact(Long parentId, Long studentId);
    void removeEmergencyContact(Long parentId, Long studentId);
}
