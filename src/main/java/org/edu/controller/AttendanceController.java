package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.AttendanceDTO;
import org.edu.dto.AttendanceSummaryDTO;
import org.edu.dto.request.BulkAttendanceRequest;
import org.edu.security.UserPrincipal;
import org.edu.service.AuditLogService;
import org.edu.service.AttendanceService;
import org.edu.service.ClassService;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
import org.edu.util.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
@Tag(name = "Attendance", description = "Manage daily attendance, bulk marking, and attendance summaries")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AuditLogService auditLogService;
    private final ClassService classService;

    @PostMapping
    @Operation(summary = "Create a single attendance record")
    public AttendanceDTO createAttendance(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody AttendanceDTO dto) {
        AttendanceDTO response = attendanceService.createAttendance(principal.getUser().getId(), dto);
        auditLogService.log(
            AuditAction.CREATE,
            AuditEntityType.ATTENDANCE,
            response.getId(),
            response.getStudentName(),
            "Created attendance record for " + response.getAttendanceDate()
        );
        return response;
    }

    @PostMapping("/bulk")
    @Operation(summary = "Mark attendance for a whole class")
    public List<AttendanceDTO> markClassAttendance(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody BulkAttendanceRequest request) {
        String className = classService.getClassById(request.getClassId()).getName();
        List<AttendanceDTO> responses = attendanceService.markClassAttendance(principal.getUser().getId(), request);
        auditLogService.log(
            AuditAction.BULK_MARK,
            AuditEntityType.ATTENDANCE,
            request.getClassId(),
            className,
            "Marked attendance for " + responses.size() + " students on " + request.getAttendanceDate()
        );
        return responses;
    }

    @GetMapping
    @Operation(summary = "List attendance records")
    public Page<AttendanceDTO> getAllAttendance(@AuthenticationPrincipal UserPrincipal principal, Pageable pageable) {
        return attendanceService.getAllAttendance(principal.getUser().getId(), pageable);
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter attendance by class, student, subject, marker, status, and date range")
    public Page<AttendanceDTO> filterAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long markedByStaffId,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable
    ) {
        return attendanceService.filterAttendance(principal.getUser().getId(), classId, studentId, subjectId, markedByStaffId, status, from, to, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an attendance record by id")
    public AttendanceDTO getAttendanceById(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return attendanceService.getAttendanceById(principal.getUser().getId(), id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an attendance record")
    public AttendanceDTO updateAttendance(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id, @Valid @RequestBody AttendanceDTO dto) {
        AttendanceDTO response = attendanceService.updateAttendance(principal.getUser().getId(), id, dto);
        auditLogService.log(
            AuditAction.UPDATE,
            AuditEntityType.ATTENDANCE,
            response.getId(),
            response.getStudentName(),
            "Updated attendance record for " + response.getAttendanceDate()
        );
        return response;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an attendance record")
    public void deleteAttendance(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        AttendanceDTO existing = attendanceService.getAttendanceById(principal.getUser().getId(), id);
        attendanceService.deleteAttendance(principal.getUser().getId(), id);
        auditLogService.log(AuditAction.DELETE, AuditEntityType.ATTENDANCE, id, existing.getStudentName(), "Deleted attendance record");
    }

    @GetMapping("/students/{studentId}")
    @Operation(summary = "List attendance records for a student")
    public Page<AttendanceDTO> getStudentAttendance(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long studentId, Pageable pageable) {
        return attendanceService.getStudentAttendance(principal.getUser().getId(), studentId, pageable);
    }

    @GetMapping("/classes/{classId}")
    @Operation(summary = "List class attendance for a specific date")
    public Page<AttendanceDTO> getClassAttendanceByDate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable
    ) {
        return attendanceService.getClassAttendanceByDate(principal.getUser().getId(), classId, date, pageable);
    }

    @GetMapping("/students/{studentId}/summary")
    @Operation(summary = "Get an attendance summary for a student within a date range")
    public AttendanceSummaryDTO getStudentAttendanceSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return attendanceService.getStudentAttendanceSummary(principal.getUser().getId(), studentId, from, to);
    }
}
