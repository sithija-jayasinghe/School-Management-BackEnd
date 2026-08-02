package org.edu.service;

import java.util.List;
import java.util.Map;
import org.edu.dto.ExamDTO;
import org.edu.dto.request.BulkExamCreateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExamService {

    ExamDTO createExam(ExamDTO dto);

    List<ExamDTO> bulkCreateExams(BulkExamCreateRequest request);

    ExamDTO updateExam(Long id, ExamDTO dto);

    void deactivateExam(Long id);

    void activateExam(Long id);

    ExamDTO getExamById(Long id);

    Page<ExamDTO> getAllExams(Pageable pageable);

    Page<ExamDTO> searchExams(String name, Pageable pageable);

    Page<ExamDTO> getExamsByClass(Long classId, Pageable pageable);

    Page<ExamDTO> filterExams(Map<String, String> filters, Pageable pageable);

    List<ExamDTO> getExamsByAcademicTerm(Long academicTermId);
}
