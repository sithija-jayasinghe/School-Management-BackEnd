package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Student Marks", description = "Manage marks entry, updates, and exam result summaries")
@SecurityRequirement(name = "bearerAuth")
public class StudentMarkController {

    private final StudentMarkService studentMarkService;

    @PostMapping
    @Operation(summary = "Create a student mark record")
    public StudentMarkDTO createStudentMark(@Valid @RequestBody StudentMarkDTO dto) {
        return studentMarkService.createStudentMark(dto);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a student mark record")
    public StudentMarkDTO updateStudentMark(@PathVariable Long id, @Valid @RequestBody StudentMarkDTO dto) {
        return studentMarkService.updateStudentMark(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a student mark record")
    public void deleteStudentMark(@PathVariable Long id) {
        studentMarkService.deleteStudentMark(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a student mark record by id")
    public StudentMarkDTO getStudentMarkById(@PathVariable Long id) {
        return studentMarkService.getStudentMarkById(id);
    }

    @GetMapping("/exams/{examId}")
    @Operation(summary = "List marks for an exam")
    public Page<StudentMarkDTO> getMarksByExam(@PathVariable Long examId, Pageable pageable) {
        return studentMarkService.getMarksByExam(examId, pageable);
    }

    @GetMapping("/exams/{examId}/summary")
    @Operation(summary = "Get the result summary for an exam")
    public ExamResultSummaryDTO getExamResultSummary(@PathVariable Long examId) {
        return studentMarkService.getExamResultSummary(examId);
    }

    @GetMapping("/students/{studentId}")
    @Operation(summary = "List marks for a student")
    public Page<StudentMarkDTO> getMarksByStudent(@PathVariable Long studentId, Pageable pageable) {
        return studentMarkService.getMarksByStudent(studentId, pageable);
    }
}
