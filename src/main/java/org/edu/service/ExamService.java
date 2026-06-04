package org.edu.service;

import java.util.List;
import org.edu.dto.ExamDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExamService {

    ExamDTO createExam(ExamDTO dto);

    ExamDTO updateExam(Long id, ExamDTO dto);

    void deactivateExam(Long id);

    void activateExam(Long id);

    ExamDTO getExamById(Long id);

    Page<ExamDTO> getAllExams(Pageable pageable);

    Page<ExamDTO> searchExams(String name, Pageable pageable);

    Page<ExamDTO> getExamsByClass(Long classId, Pageable pageable);

    List<ExamDTO> getExamsByAcademicTerm(Long academicTermId);
}
