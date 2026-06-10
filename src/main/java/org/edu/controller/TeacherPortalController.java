package org.edu.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.AcademicReportDTO;
import org.edu.dto.AttendanceDTO;
import org.edu.dto.AttendanceSummaryDTO;
import org.edu.dto.DocumentFileResponse;
import org.edu.dto.LeaveRequestDTO;
import org.edu.dto.teacherportal.TeacherPortalClassSummaryDTO;
import org.edu.dto.teacherportal.TeacherPortalAcademicReportGenerateRequest;
import org.edu.dto.teacherportal.TeacherPortalBulkAttendanceRequest;
import org.edu.dto.teacherportal.TeacherPortalDashboardDTO;
import org.edu.dto.teacherportal.TeacherPortalDocumentCreateRequest;
import org.edu.dto.teacherportal.TeacherPortalDocumentDTO;
import org.edu.dto.teacherportal.TeacherPortalExamDTO;
import org.edu.dto.teacherportal.TeacherPortalLeaveReviewRequest;
import org.edu.dto.teacherportal.TeacherPortalProfileDTO;
import org.edu.dto.teacherportal.TeacherPortalStudentDTO;
import org.edu.dto.teacherportal.TeacherPortalSubjectDTO;
import org.edu.dto.teacherportal.TeacherPortalTimetableEntryDTO;
import org.edu.security.UserPrincipal;
import org.edu.service.TeacherPortalService;
import org.edu.util.LeaveRequestStatus;
import org.edu.dto.request.AcademicReportUpdateRequest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/teacher-portal")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherPortalController {

    private final TeacherPortalService teacherPortalService;

    @GetMapping("/profile")
    public TeacherPortalProfileDTO getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getProfile(principal.getUser().getId());
    }

    @GetMapping("/dashboard")
    public TeacherPortalDashboardDTO getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getDashboard(principal.getUser().getId());
    }

    @GetMapping("/classes")
    public List<TeacherPortalClassSummaryDTO> getAssignedClasses(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getAssignedClasses(principal.getUser().getId());
    }

    @GetMapping("/schedule")
    public List<TeacherPortalTimetableEntryDTO> getSchedule(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getSchedule(principal.getUser().getId());
    }

    @GetMapping("/subjects")
    public List<TeacherPortalSubjectDTO> getSubjects(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getSubjects(principal.getUser().getId());
    }

    @GetMapping("/classes/{classId}/students")
    public List<TeacherPortalStudentDTO> getClassStudents(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long classId
    ) {
        return teacherPortalService.getClassStudents(principal.getUser().getId(), classId);
    }

    @GetMapping("/exams")
    public List<TeacherPortalExamDTO> getExams(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getExams(principal.getUser().getId());
    }

    @GetMapping("/classes/{classId}/attendance")
    public Page<AttendanceDTO> getClassAttendanceByDate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable
    ) {
        return teacherPortalService.getClassAttendanceByDate(principal.getUser().getId(), classId, date, pageable);
    }

    @PostMapping("/classes/{classId}/attendance")
    public List<AttendanceDTO> markClassAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long classId,
            @Valid @RequestBody TeacherPortalBulkAttendanceRequest request
    ) {
        return teacherPortalService.markClassAttendance(principal.getUser().getId(), classId, request);
    }

    @GetMapping("/students/{studentId}/attendance")
    public Page<AttendanceDTO> getStudentAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            Pageable pageable
    ) {
        return teacherPortalService.getStudentAttendance(principal.getUser().getId(), studentId, pageable);
    }

    @GetMapping("/students/{studentId}/attendance/summary")
    public AttendanceSummaryDTO getStudentAttendanceSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return teacherPortalService.getStudentAttendanceSummary(principal.getUser().getId(), studentId, from, to);
    }

    @GetMapping("/students/{studentId}/documents")
    public Page<TeacherPortalDocumentDTO> getStudentDocuments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            Pageable pageable
    ) {
        return teacherPortalService.getStudentDocuments(principal.getUser().getId(), studentId, pageable);
    }

    @PostMapping(value = "/students/{studentId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TeacherPortalDocumentDTO uploadStudentDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            @Valid @RequestPart("metadata") TeacherPortalDocumentCreateRequest request,
            @RequestPart("file") MultipartFile file
    ) {
        return teacherPortalService.uploadStudentDocument(principal.getUser().getId(), studentId, request, file);
    }

    @GetMapping("/students/{studentId}/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadStudentDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            @PathVariable Long documentId
    ) {
        DocumentFileResponse fileResponse = teacherPortalService.downloadStudentDocument(
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

    @PostMapping("/students/{studentId}/academic-reports")
    public AcademicReportDTO generateStudentAcademicReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            @Valid @RequestBody TeacherPortalAcademicReportGenerateRequest request
    ) {
        return teacherPortalService.generateStudentAcademicReport(
                principal.getUser().getId(),
                studentId,
                request
        );
    }

    @GetMapping("/students/{studentId}/academic-reports")
    public Page<AcademicReportDTO> getStudentAcademicReports(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            Pageable pageable
    ) {
        return teacherPortalService.getStudentAcademicReports(principal.getUser().getId(), studentId, pageable);
    }

    @GetMapping("/classes/{classId}/academic-reports")
    public Page<AcademicReportDTO> getClassAcademicReports(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long classId,
            @RequestParam Long academicTermId,
            Pageable pageable
    ) {
        return teacherPortalService.getClassAcademicReports(
                principal.getUser().getId(),
                classId,
                academicTermId,
                pageable
        );
    }

    @PostMapping("/academic-reports/{reportId}/regenerate")
    public AcademicReportDTO regenerateAcademicReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        return teacherPortalService.regenerateAcademicReport(principal.getUser().getId(), reportId);
    }

    @PatchMapping("/academic-reports/{reportId}")
    public AcademicReportDTO updateAcademicReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId,
            @RequestBody AcademicReportUpdateRequest request
    ) {
        return teacherPortalService.updateAcademicReport(principal.getUser().getId(), reportId, request);
    }

    @PostMapping("/academic-reports/{reportId}/publish")
    public AcademicReportDTO publishAcademicReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        return teacherPortalService.publishAcademicReport(principal.getUser().getId(), reportId);
    }

    @GetMapping("/academic-reports/{reportId}/pdf")
    public ResponseEntity<Resource> downloadAcademicReportCard(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        DocumentFileResponse file = teacherPortalService.downloadAcademicReportCard(
                principal.getUser().getId(),
                reportId
        );
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(file.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .body(file.getResource());
    }

    @GetMapping("/leave-requests")
    public Page<LeaveRequestDTO> getLeaveRequests(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) LeaveRequestStatus status,
            Pageable pageable
    ) {
        return teacherPortalService.getLeaveRequests(principal.getUser().getId(), status, pageable);
    }

    @PostMapping("/leave-requests/{leaveRequestId}/approve")
    public LeaveRequestDTO approveLeaveRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long leaveRequestId,
            @RequestBody(required = false) TeacherPortalLeaveReviewRequest request
    ) {
        return teacherPortalService.approveLeaveRequest(principal.getUser().getId(), leaveRequestId, request);
    }

    @PostMapping("/leave-requests/{leaveRequestId}/reject")
    public LeaveRequestDTO rejectLeaveRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long leaveRequestId,
            @RequestBody(required = false) TeacherPortalLeaveReviewRequest request
    ) {
        return teacherPortalService.rejectLeaveRequest(principal.getUser().getId(), leaveRequestId, request);
    }

    @PostMapping("/leave-requests/{leaveRequestId}/apply-attendance")
    public LeaveRequestDTO applyLeaveToAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long leaveRequestId
    ) {
        return teacherPortalService.applyLeaveToAttendance(principal.getUser().getId(), leaveRequestId);
    }
}
