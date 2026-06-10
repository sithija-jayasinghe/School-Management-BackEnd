package org.edu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.AcademicReportDTO;
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
public class AcademicReportController {

    private final AcademicReportService academicReportService;

    @PostMapping
    public AcademicReportDTO generateReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AcademicReportGenerateRequest request
    ) {
        return academicReportService.generateReport(principal.getUser().getId(), request);
    }

    @PostMapping("/{reportId}/regenerate")
    public AcademicReportDTO regenerateReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        return academicReportService.regenerateReport(principal.getUser().getId(), reportId);
    }

    @PatchMapping("/{reportId}")
    public AcademicReportDTO updateReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId,
            @Valid @RequestBody AcademicReportUpdateRequest request
    ) {
        return academicReportService.updateReport(principal.getUser().getId(), reportId, request);
    }

    @PostMapping("/{reportId}/publish")
    public AcademicReportDTO publishReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        return academicReportService.publishReport(principal.getUser().getId(), reportId);
    }

    @DeleteMapping("/{reportId}")
    public void deleteDraftReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        academicReportService.deleteDraftReport(principal.getUser().getId(), reportId);
    }

    @GetMapping("/{reportId}")
    public AcademicReportDTO getReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        return academicReportService.getReport(principal.getUser().getId(), reportId);
    }

    @GetMapping("/students/{studentId}")
    public Page<AcademicReportDTO> getStudentReports(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            Pageable pageable
    ) {
        return academicReportService.getStudentReports(principal.getUser().getId(), studentId, pageable);
    }

    @GetMapping("/classes/{classId}")
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
