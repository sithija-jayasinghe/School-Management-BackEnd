package org.edu.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.ExamDTO;
import org.edu.service.ExamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class ExamController {

    private final ExamService examService;

    @PostMapping
    public ExamDTO createExam(@Valid @RequestBody ExamDTO dto) {
        return examService.createExam(dto);
    }

    @PatchMapping("/{id}")
    public ExamDTO updateExam(@PathVariable Long id, @RequestBody ExamDTO dto) {
        return examService.updateExam(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deactivateExam(@PathVariable Long id) {
        examService.deactivateExam(id);
    }

    @PostMapping("/{id}/activate")
    public void activateExam(@PathVariable Long id) {
        examService.activateExam(id);
    }

    @GetMapping
    public Page<ExamDTO> getAllExams(Pageable pageable) {
        return examService.getAllExams(pageable);
    }

    @GetMapping("/{id}")
    public ExamDTO getExamById(@PathVariable Long id) {
        return examService.getExamById(id);
    }

    @GetMapping("/search")
    public Page<ExamDTO> searchExams(@RequestParam String name, Pageable pageable) {
        return examService.searchExams(name, pageable);
    }

    @GetMapping("/classes/{classId}")
    public Page<ExamDTO> getExamsByClass(@PathVariable Long classId, Pageable pageable) {
        return examService.getExamsByClass(classId, pageable);
    }

    @GetMapping("/academic-terms/{academicTermId}")
    public List<ExamDTO> getExamsByAcademicTerm(@PathVariable Long academicTermId) {
        return examService.getExamsByAcademicTerm(academicTermId);
    }
}
