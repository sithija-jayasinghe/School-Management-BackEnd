package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.edu.dto.AcademicReportDTO;
import org.edu.dto.LeaveRequestDTO;
import org.edu.dto.DocumentFileResponse;
import lombok.RequiredArgsConstructor;
import org.edu.dto.parentportal.ParentPortalAttendanceDTO;
import org.edu.dto.parentportal.ParentPortalDashboardDTO;
import org.edu.dto.parentportal.ParentPortalDocumentDTO;
import org.edu.dto.parentportal.ParentPortalLeaveRequestCreateDTO;
import org.edu.dto.parentportal.ParentPortalNoticeDTO;
import org.edu.dto.parentportal.ParentPortalProfileDTO;
import org.edu.dto.parentportal.ParentPortalResultDTO;
import org.edu.dto.parentportal.ParentPortalStudentDetailDTO;
import org.edu.dto.parentportal.ParentPortalStudentSummaryDTO;
import org.edu.dto.parentportal.ParentPortalSubjectDTO;
import org.edu.dto.parentportal.ParentPortalTimetableEntryDTO;
import org.edu.security.UserPrincipal;
import org.edu.service.ParentPortalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parent-portal")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARENT')")
@Tag(name = "Parent Portal", description = "Parent-facing portal APIs for linked students, notices, leave, and report access")
@SecurityRequirement(name = "bearerAuth")
public class ParentPortalController {

    private final ParentPortalService parentPortalService;

    @GetMapping("/profile")
    @Operation(summary = "Get the authenticated parent profile")
    public ParentPortalProfileDTO getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return parentPortalService.getProfile(principal.getUser().getId());
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get the parent portal dashboard")
    public ParentPortalDashboardDTO getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return parentPortalService.getDashboard(principal.getUser().getId());
    }

    @GetMapping("/notices")
    @Operation(summary = "List notices visible to the parent")
    public List<ParentPortalNoticeDTO> getNotices(@AuthenticationPrincipal UserPrincipal principal) {
        return parentPortalService.getNotices(principal.getUser().getId());
    }

    @GetMapping("/students")
    @Operation(summary = "List students linked to the authenticated parent")
    public List<ParentPortalStudentSummaryDTO> getLinkedStudents(@AuthenticationPrincipal UserPrincipal principal) {
        return parentPortalService.getLinkedStudents(principal.getUser().getId());
    }

    @GetMapping("/students/{studentId}")
    @Operation(summary = "Get linked student details")
    public ParentPortalStudentDetailDTO getStudentDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId
    ) {
        return parentPortalService.getStudentDetail(principal.getUser().getId(), studentId);
    }

    @GetMapping("/students/{studentId}/subjects")
    @Operation(summary = "List subjects for a linked student")
    public List<ParentPortalSubjectDTO> getStudentSubjects(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId
    ) {
        return parentPortalService.getStudentSubjects(principal.getUser().getId(), studentId);
    }

    @GetMapping("/students/{studentId}/timetable")
    @Operation(summary = "Get timetable entries for a linked student")
    public List<ParentPortalTimetableEntryDTO> getStudentTimetable(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId
    ) {
        return parentPortalService.getStudentTimetable(principal.getUser().getId(), studentId);
    }

    @GetMapping("/students/{studentId}/attendance")
    @Operation(summary = "Get attendance history for a linked student within a date range")
    public List<ParentPortalAttendanceDTO> getStudentAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return parentPortalService.getStudentAttendance(principal.getUser().getId(), studentId, from, to);
    }

    @GetMapping("/students/{studentId}/results")
    @Operation(summary = "Get exam results for a linked student")
    public List<ParentPortalResultDTO> getStudentResults(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId
    ) {
        return parentPortalService.getStudentResults(principal.getUser().getId(), studentId);
    }

    @GetMapping("/students/{studentId}/documents")
    @Operation(summary = "List parent-visible documents for a linked student")
    public Page<ParentPortalDocumentDTO> getStudentDocuments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            Pageable pageable
    ) {
        return parentPortalService.getStudentDocuments(principal.getUser().getId(), studentId, pageable);
    }

    @GetMapping("/students/{studentId}/documents/{documentId}/download")
    @Operation(summary = "Download a parent-visible student document")
    public ResponseEntity<Resource> downloadStudentDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            @PathVariable Long documentId
    ) {
        DocumentFileResponse fileResponse = parentPortalService.downloadStudentDocument(
                principal.getUser().getId(),
                studentId,
                documentId
        );
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileResponse.getContentType()))
                .contentLength(fileResponse.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResponse.getFileName() + "\"")
                .body(fileResponse.getResource());
    }

    @GetMapping("/students/{studentId}/academic-reports")
    @Operation(summary = "List published academic reports for a linked student")
    public Page<AcademicReportDTO> getStudentAcademicReports(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            Pageable pageable
    ) {
        return parentPortalService.getStudentAcademicReports(principal.getUser().getId(), studentId, pageable);
    }

    @GetMapping("/students/{studentId}/academic-reports/{reportId}")
    @Operation(summary = "Get a published academic report for a linked student")
    public AcademicReportDTO getStudentAcademicReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            @PathVariable Long reportId
    ) {
        return parentPortalService.getStudentAcademicReport(principal.getUser().getId(), studentId, reportId);
    }

    @GetMapping("/students/{studentId}/academic-reports/{reportId}/pdf")
    @Operation(summary = "Download a linked student's report card PDF")
    public ResponseEntity<Resource> downloadStudentReportCard(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            @PathVariable Long reportId
    ) {
        DocumentFileResponse fileResponse = parentPortalService.downloadStudentReportCard(
                principal.getUser().getId(),
                studentId,
                reportId
        );
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(fileResponse.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResponse.getFileName() + "\"")
                .body(fileResponse.getResource());
    }

    @PostMapping("/leave-requests")
    @Operation(summary = "Create a leave request from the parent portal")
    public LeaveRequestDTO createLeaveRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ParentPortalLeaveRequestCreateDTO dto
    ) {
        return parentPortalService.createLeaveRequest(principal.getUser().getId(), dto);
    }

    @GetMapping("/leave-requests")
    @Operation(summary = "List the parent's leave requests")
    public Page<LeaveRequestDTO> getLeaveRequests(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable
    ) {
        return parentPortalService.getLeaveRequests(principal.getUser().getId(), pageable);
    }

    @PostMapping("/leave-requests/{leaveRequestId}/cancel")
    @Operation(summary = "Cancel a leave request from the parent portal")
    public LeaveRequestDTO cancelLeaveRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long leaveRequestId,
            @RequestParam(required = false) String remarks
    ) {
        return parentPortalService.cancelLeaveRequest(principal.getUser().getId(), leaveRequestId, remarks);
    }
}
