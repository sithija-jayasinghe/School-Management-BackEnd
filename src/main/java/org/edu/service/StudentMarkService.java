package org.edu.service;

import java.util.List;
import org.edu.dto.ExamResultSummaryDTO;
import org.edu.dto.StudentMarkDTO;
import org.edu.dto.parentportal.ParentPortalResultDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentMarkService {

    StudentMarkDTO createStudentMark(Long authenticatedUserId, StudentMarkDTO dto);

    StudentMarkDTO updateStudentMark(Long authenticatedUserId, Long id, StudentMarkDTO dto);

    void deleteStudentMark(Long authenticatedUserId, Long id);

    StudentMarkDTO getStudentMarkById(Long id);

    Page<StudentMarkDTO> getMarksByExam(Long examId, Pageable pageable);

    Page<StudentMarkDTO> getMarksByStudent(Long studentId, Pageable pageable);

    ExamResultSummaryDTO getExamResultSummary(Long examId);

    List<ParentPortalResultDTO> getPortalResults(Long studentId);
}
