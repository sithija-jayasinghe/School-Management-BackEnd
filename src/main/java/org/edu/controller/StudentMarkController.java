package org.edu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.ExamResultSummaryDTO;
import org.edu.dto.StudentMarkDTO;
import org.edu.service.StudentMarkService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student-marks")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class StudentMarkController {

    private final StudentMarkService studentMarkService;

    @PostMapping
    public StudentMarkDTO createStudentMark(@Valid @RequestBody StudentMarkDTO dto) {
        return studentMarkService.createStudentMark(dto);
    }

    @PatchMapping("/{id}")
    public StudentMarkDTO updateStudentMark(@PathVariable Long id, @RequestBody StudentMarkDTO dto) {
        return studentMarkService.updateStudentMark(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteStudentMark(@PathVariable Long id) {
        studentMarkService.deleteStudentMark(id);
    }

    @GetMapping("/{id}")
    public StudentMarkDTO getStudentMarkById(@PathVariable Long id) {
        return studentMarkService.getStudentMarkById(id);
    }

    @GetMapping("/exams/{examId}")
    public Page<StudentMarkDTO> getMarksByExam(@PathVariable Long examId, Pageable pageable) {
        return studentMarkService.getMarksByExam(examId, pageable);
    }

    @GetMapping("/exams/{examId}/summary")
    public ExamResultSummaryDTO getExamResultSummary(@PathVariable Long examId) {
        return studentMarkService.getExamResultSummary(examId);
    }

    @GetMapping("/students/{studentId}")
    public Page<StudentMarkDTO> getMarksByStudent(@PathVariable Long studentId, Pageable pageable) {
        return studentMarkService.getMarksByStudent(studentId, pageable);
    }
}
