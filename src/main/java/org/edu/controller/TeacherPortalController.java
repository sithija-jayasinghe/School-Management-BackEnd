package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.AcademicReportDTO;
import org.edu.dto.AcademicReportReadinessDTO;
import org.edu.dto.AcademicTermDTO;
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
import org.edu.dto.teacherportal.TeacherPortalSchoolDayHoursDTO;
import org.edu.dto.teacherleave.TeacherLeaveRequestDTO;
import org.edu.dto.teacherleave.TeacherLeaveSaveRequest;
import org.edu.security.UserPrincipal;
import org.edu.service.AcademicTermService;
import org.edu.service.TeacherPortalService;
import org.edu.service.TeacherLeaveService;
import org.edu.service.SchoolDayPolicyService;
import org.edu.util.TeacherLeaveStatus;
import org.edu.util.TeacherLeaveType;
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
@Tag(name = "Teacher Portal", description = "Teacher-facing portal APIs for classes, attendance, leave, documents, and reports")
@SecurityRequirement(name = "bearerAuth")
public class TeacherPortalController {

    private final TeacherPortalService teacherPortalService;
    private final AcademicTermService academicTermService;
    private final TeacherLeaveService teacherLeaveService;
    private final SchoolDayPolicyService schoolDayPolicyService;

    @GetMapping("/profile")
    @Operation(summary = "Get the authenticated teacher profile")
    public TeacherPortalProfileDTO getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getProfile(principal.getUser().getId());
    }

    @GetMapping("/school-day-hours")
    @Operation(summary = "Get the configured school operating hours")
    public TeacherPortalSchoolDayHoursDTO getSchoolDayHours() {
        return new TeacherPortalSchoolDayHoursDTO(
                schoolDayPolicyService.getSchoolStartTime(),
                schoolDayPolicyService.getSchoolEndTime()
        );
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get the teacher portal dashboard")
    public TeacherPortalDashboardDTO getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getDashboard(principal.getUser().getId());
    }

    @GetMapping("/classes")
    @Operation(summary = "List classes assigned to the teacher")
    public List<TeacherPortalClassSummaryDTO> getAssignedClasses(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getAssignedClasses(principal.getUser().getId());
    }

    @GetMapping("/schedule")
    @Operation(summary = "Get the teacher timetable")
    public List<TeacherPortalTimetableEntryDTO> getSchedule(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getSchedule(principal.getUser().getId());
    }

    @GetMapping("/subjects")
    @Operation(summary = "List subjects handled by the teacher")
    public List<TeacherPortalSubjectDTO> getSubjects(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getSubjects(principal.getUser().getId());
    }

    @GetMapping("/classes/{classId}/students")
    @Operation(summary = "List students in an assigned class")
    public List<TeacherPortalStudentDTO> getClassStudents(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long classId
    ) {
        return teacherPortalService.getClassStudents(principal.getUser().getId(), classId);
    }

    @GetMapping("/exams")
    @Operation(summary = "List exams relevant to the teacher")
    public List<TeacherPortalExamDTO> getExams(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getExams(principal.getUser().getId());
    }

    @GetMapping("/academic-terms")
    @Operation(summary = "List active academic terms for teacher report generation")
    public List<AcademicTermDTO> getActiveAcademicTerms() {
        return academicTermService.getAllActiveAcademicTerms();
    }

    @GetMapping("/classes/{classId}/attendance")
    @Operation(summary = "Get attendance for an assigned class on a given date")
    public Page<AttendanceDTO> getClassAttendanceByDate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable
    ) {
        return teacherPortalService.getClassAttendanceByDate(principal.getUser().getId(), classId, date, pageable);
    }

    @PostMapping("/classes/{classId}/attendance")
    @Operation(summary = "Mark attendance for an assigned class")
    public List<AttendanceDTO> markClassAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long classId,
            @Valid @RequestBody TeacherPortalBulkAttendanceRequest request
    ) {
        return teacherPortalService.markClassAttendance(principal.getUser().getId(), classId, request);
    }

    @GetMapping("/students/{studentId}/attendance")
    @Operation(summary = "List attendance records for a student in the teacher's scope")
    public Page<AttendanceDTO> getStudentAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            Pageable pageable
    ) {
        return teacherPortalService.getStudentAttendance(principal.getUser().getId(), studentId, pageable);
    }

    @GetMapping("/students/{studentId}/attendance/summary")
    @Operation(summary = "Get attendance summary for a student in the teacher's scope")
    public AttendanceSummaryDTO getStudentAttendanceSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return teacherPortalService.getStudentAttendanceSummary(principal.getUser().getId(), studentId, from, to);
    }

    @GetMapping("/students/{studentId}/documents")
    @Operation(summary = "List documents for a student in the teacher's scope")
    public Page<TeacherPortalDocumentDTO> getStudentDocuments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            Pageable pageable
    ) {
        return teacherPortalService.getStudentDocuments(principal.getUser().getId(), studentId, pageable);
    }

    @PostMapping(value = "/students/{studentId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document for a student in the teacher's scope")
    public TeacherPortalDocumentDTO uploadStudentDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            @Valid @RequestPart("metadata") TeacherPortalDocumentCreateRequest request,
            @RequestPart("file") MultipartFile file
    ) {
        return teacherPortalService.uploadStudentDocument(principal.getUser().getId(), studentId, request, file);
    }

    @GetMapping("/students/{studentId}/documents/{documentId}/download")
    @Operation(summary = "Download a document for a student in the teacher's scope")
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
    @Operation(summary = "Generate an academic report for a student in the teacher's scope")
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

    @GetMapping("/students/{studentId}/academic-reports/readiness")
    @Operation(summary = "Check report readiness for a student in the teacher's scope")
    public AcademicReportReadinessDTO checkStudentAcademicReportReadiness(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            @RequestParam Long academicTermId
    ) {
        return teacherPortalService.checkStudentAcademicReportReadiness(
                principal.getUser().getId(),
                studentId,
                academicTermId
        );
    }

    @GetMapping("/students/{studentId}/academic-reports")
    @Operation(summary = "List academic reports for a student in the teacher's scope")
    public Page<AcademicReportDTO> getStudentAcademicReports(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            Pageable pageable
    ) {
        return teacherPortalService.getStudentAcademicReports(principal.getUser().getId(), studentId, pageable);
    }

    @GetMapping("/classes/{classId}/academic-reports")
    @Operation(summary = "List class academic reports for a teacher-managed class")
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
    @Operation(summary = "Regenerate an academic report from the teacher portal")
    public AcademicReportDTO regenerateAcademicReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        return teacherPortalService.regenerateAcademicReport(principal.getUser().getId(), reportId);
    }

    @PatchMapping("/academic-reports/{reportId}")
    @Operation(summary = "Update academic report remarks from the teacher portal")
    public AcademicReportDTO updateAcademicReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId,
            @Valid @RequestBody AcademicReportUpdateRequest request
    ) {
        return teacherPortalService.updateAcademicReport(principal.getUser().getId(), reportId, request);
    }

    @PostMapping("/academic-reports/{reportId}/publish")
    @Operation(summary = "Publish an academic report from the teacher portal")
    public AcademicReportDTO publishAcademicReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        return teacherPortalService.publishAcademicReport(principal.getUser().getId(), reportId);
    }

    @GetMapping("/academic-reports/{reportId}/pdf")
    @Operation(summary = "Download an academic report card PDF from the teacher portal")
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
    @Operation(summary = "List leave requests relevant to the teacher")
    public Page<LeaveRequestDTO> getLeaveRequests(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) LeaveRequestStatus status,
            Pageable pageable
    ) {
        return teacherPortalService.getLeaveRequests(principal.getUser().getId(), status, pageable);
    }

    @PostMapping("/leave-requests/{leaveRequestId}/approve")
    @Operation(summary = "Approve a leave request from the teacher portal")
    public LeaveRequestDTO approveLeaveRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long leaveRequestId,
            @Valid @RequestBody(required = false) TeacherPortalLeaveReviewRequest request
    ) {
        return teacherPortalService.approveLeaveRequest(principal.getUser().getId(), leaveRequestId, request);
    }

    @PostMapping("/leave-requests/{leaveRequestId}/reject")
    @Operation(summary = "Reject a leave request from the teacher portal")
    public LeaveRequestDTO rejectLeaveRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long leaveRequestId,
            @Valid @RequestBody(required = false) TeacherPortalLeaveReviewRequest request
    ) {
        return teacherPortalService.rejectLeaveRequest(principal.getUser().getId(), leaveRequestId, request);
    }

    @PostMapping("/leave-requests/{leaveRequestId}/apply-attendance")
    @Operation(summary = "Apply an approved leave request to attendance records")
    public LeaveRequestDTO applyLeaveToAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long leaveRequestId
    ) {
        return teacherPortalService.applyLeaveToAttendance(principal.getUser().getId(), leaveRequestId);
    }

    @PostMapping("/my-leave-requests")
    @Operation(summary = "Submit leave for the authenticated teacher")
    public TeacherLeaveRequestDTO submitOwnLeave(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TeacherLeaveSaveRequest request
    ) {
        return teacherLeaveService.submit(principal.getUser().getId(), request);
    }

    @GetMapping("/my-leave-requests")
    @Operation(summary = "List leave submitted by the authenticated teacher")
    public Page<TeacherLeaveRequestDTO> getOwnLeaveRequests(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) TeacherLeaveStatus status,
            @RequestParam(required = false) TeacherLeaveType leaveType,
            Pageable pageable
    ) {
        return teacherLeaveService.getOwnRequests(principal.getUser().getId(), status, leaveType, pageable);
    }

    @GetMapping("/my-leave-requests/{requestId}")
    @Operation(summary = "Get one leave request owned by the authenticated teacher")
    public TeacherLeaveRequestDTO getOwnLeaveRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long requestId
    ) {
        return teacherLeaveService.getOwnRequest(principal.getUser().getId(), requestId);
    }

    @PatchMapping("/my-leave-requests/{requestId}")
    @Operation(summary = "Update a pending leave request owned by the authenticated teacher")
    public TeacherLeaveRequestDTO updateOwnLeave(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long requestId,
            @Valid @RequestBody TeacherLeaveSaveRequest request
    ) {
        return teacherLeaveService.updateOwnRequest(principal.getUser().getId(), requestId, request);
    }

    @PostMapping("/my-leave-requests/{requestId}/cancel")
    @Operation(summary = "Cancel a pending leave request owned by the authenticated teacher")
    public TeacherLeaveRequestDTO cancelOwnLeave(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long requestId
    ) {
        return teacherLeaveService.cancelOwnRequest(principal.getUser().getId(), requestId);
    }
}
