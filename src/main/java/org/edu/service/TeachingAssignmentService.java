package org.edu.service;

import java.util.List;
import org.edu.dto.ClassTeacherAssignmentRequest;
import org.edu.dto.TeachingAssignmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeachingAssignmentService {

    TeachingAssignmentDTO createAssignment(TeachingAssignmentDTO dto);

    List<TeachingAssignmentDTO> assignClassTeacher(ClassTeacherAssignmentRequest request);

    TeachingAssignmentDTO updateAssignment(Long id, TeachingAssignmentDTO dto);

    void deactivateAssignment(Long id);

    TeachingAssignmentDTO getAssignmentById(Long id);

    Page<TeachingAssignmentDTO> getAssignments(Pageable pageable);

    Page<TeachingAssignmentDTO> searchAssignments(String keyword, Pageable pageable);

    List<TeachingAssignmentDTO> getAssignmentsByTeacher(Long staffId);

    List<TeachingAssignmentDTO> getAssignmentsByClass(Long classId);
}
