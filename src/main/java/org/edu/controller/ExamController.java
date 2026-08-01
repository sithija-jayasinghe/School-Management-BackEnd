package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.edu.dto.ExamDTO;
import org.edu.dto.request.BulkExamCreateRequest;
import org.edu.service.AuditLogService;
import org.edu.service.ExamService;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
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
@Tag(name = "Exams", description = "Manage exams and exam queries by class and academic term")
@SecurityRequirement(name = "bearerAuth")
public class ExamController {

    private final ExamService examService;
    private final AuditLogService auditLogService;

    @PostMapping
    @Operation(summary = "Create an exam")
    public ExamDTO createExam(@Valid @RequestBody ExamDTO dto) {
        ExamDTO response = examService.createExam(dto);
        auditLogService.log(AuditAction.CREATE, AuditEntityType.EXAM, response.getId(), response.getName(), "Created exam");
        return response;
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create exams for multiple subjects in one class/term at once")
    public List<ExamDTO> bulkCreateExams(@Valid @RequestBody BulkExamCreateRequest request) {
        List<ExamDTO> responses = examService.bulkCreateExams(request);
        auditLogService.log(
                AuditAction.CREATE,
                AuditEntityType.EXAM,
                null,
                request.getSubjectIds().size() + " subjects",
                "Bulk created " + responses.size() + " exams for class/term"
        );
        return responses;
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an exam")
    public ExamDTO updateExam(@PathVariable Long id, @Valid @RequestBody ExamDTO dto) {
        ExamDTO response = examService.updateExam(id, dto);
        auditLogService.log(AuditAction.UPDATE, AuditEntityType.EXAM, response.getId(), response.getName(), "Updated exam");
        return response;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate an exam")
    public void deactivateExam(@PathVariable Long id) {
        examService.deactivateExam(id);
        auditLogService.log(AuditAction.DEACTIVATE, AuditEntityType.EXAM, id, "Exam #" + id, "Deactivated exam");
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate an exam")
    public void activateExam(@PathVariable Long id) {
        examService.activateExam(id);
        auditLogService.log(AuditAction.ACTIVATE, AuditEntityType.EXAM, id, "Exam #" + id, "Activated exam");
    }

    @GetMapping
    @Operation(summary = "List exams")
    public Page<ExamDTO> getAllExams(Pageable pageable) {
        return examService.getAllExams(pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Search exams by name")
    public Page<ExamDTO> searchExams(@RequestParam String name, Pageable pageable) {
        return examService.searchExams(name, pageable);
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter exams by text, academic period, class, subject, type, status, and date range")
    public Page<ExamDTO> filterExams(@RequestParam Map<String, String> filters, Pageable pageable) {
        return examService.filterExams(filters, pageable);
    }

    @GetMapping("/classes/{classId}")
    @Operation(summary = "List exams for a class")
    public Page<ExamDTO> getExamsByClass(@PathVariable Long classId, Pageable pageable) {
        return examService.getExamsByClass(classId, pageable);
    }

    @GetMapping("/academic-terms/{academicTermId}")
    @Operation(summary = "List exams for an academic term")
    public List<ExamDTO> getExamsByAcademicTerm(@PathVariable Long academicTermId) {
        return examService.getExamsByAcademicTerm(academicTermId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an exam by id")
    public ExamDTO getExamById(@PathVariable Long id) {
        return examService.getExamById(id);
    }
}
