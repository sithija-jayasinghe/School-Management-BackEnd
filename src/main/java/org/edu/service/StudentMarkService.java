package org.edu.service;

import org.edu.dto.StudentMarkDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentMarkService {

    StudentMarkDTO createStudentMark(StudentMarkDTO dto);

    StudentMarkDTO updateStudentMark(Long id, StudentMarkDTO dto);

    void deleteStudentMark(Long id);

    StudentMarkDTO getStudentMarkById(Long id);

    Page<StudentMarkDTO> getMarksByExam(Long examId, Pageable pageable);

    Page<StudentMarkDTO> getMarksByStudent(Long studentId, Pageable pageable);
}
