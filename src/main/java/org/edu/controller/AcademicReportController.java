package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.AcademicReportDTO;
import org.edu.dto.AcademicReportReadinessDTO;
import org.edu.dto.DocumentFileResponse;
import org.edu.dto.request.AcademicReportGenerateRequest;
import org.edu.dto.request.AcademicReportUpdateRequest;
import org.edu.security.UserPrincipal;
import org.edu.service.AcademicReportService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/academic-reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
@Tag(name = "Academic Reports", description = "Generate, publish, view, and download academic reports and report cards")
@SecurityRequirement(name = "bearerAuth")
public class AcademicReportController {

    private final AcademicReportService academicReportService;

    @GetMapping("/readiness")
    @Operation(summary = "Check if report prerequisites are ready")
    public AcademicReportReadinessDTO checkReportReadiness(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Long studentId,
            @RequestParam Long academicTermId
    ) {
        return academicReportService.checkReportReadiness(
                principal.getUser().getId(),
                studentId,
                academicTermId
        );
    }

    @PostMapping
    @Operation(summary = "Generate an academic report")
    public AcademicReportDTO generateReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AcademicReportGenerateRequest request
    ) {
        return academicReportService.generateReport(principal.getUser().getId(), request);
    }

    @PostMapping("/{reportId}/regenerate")
    @Operation(summary = "Regenerate an academic report")
    public AcademicReportDTO regenerateReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        return academicReportService.regenerateReport(principal.getUser().getId(), reportId);
    }

    @PatchMapping("/{reportId}")
    @Operation(summary = "Update academic report remarks")
    public AcademicReportDTO updateReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId,
            @Valid @RequestBody AcademicReportUpdateRequest request
    ) {
        return academicReportService.updateReport(principal.getUser().getId(), reportId, request);
    }

    @PostMapping("/{reportId}/publish")
    @Operation(summary = "Publish an academic report")
    public AcademicReportDTO publishReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        return academicReportService.publishReport(principal.getUser().getId(), reportId);
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "Delete a draft academic report")
    public void deleteDraftReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        academicReportService.deleteDraftReport(principal.getUser().getId(), reportId);
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Get an academic report by id")
    public AcademicReportDTO getReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        return academicReportService.getReport(principal.getUser().getId(), reportId);
    }

    @GetMapping("/students/{studentId}")
    @Operation(summary = "List academic reports for a student")
    public Page<AcademicReportDTO> getStudentReports(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            Pageable pageable
    ) {
        return academicReportService.getStudentReports(principal.getUser().getId(), studentId, pageable);
    }

    @GetMapping("/classes/{classId}")
    @Operation(summary = "List academic reports for a class and academic term")
    public Page<AcademicReportDTO> getClassReports(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long classId,
            @RequestParam Long academicTermId,
            Pageable pageable
    ) {
        return academicReportService.getClassReports(
                principal.getUser().getId(),
                classId,
                academicTermId,
                pageable
        );
    }

    @GetMapping("/{reportId}/pdf")
    @Operation(summary = "Download an academic report card as PDF")
    public ResponseEntity<Resource> downloadReportCard(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        DocumentFileResponse file = academicReportService.downloadReportCard(
                principal.getUser().getId(),
                reportId
        );
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(file.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .body(file.getResource());
    }
}
