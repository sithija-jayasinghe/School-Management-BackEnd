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
import org.edu.service.AuditLogService;
import org.edu.service.AttendanceService;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
@Tag(name = "Attendance", description = "Manage daily attendance, bulk marking, and attendance summaries")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AuditLogService auditLogService;

    @PostMapping
    @Operation(summary = "Create a single attendance record")
    public AttendanceDTO createAttendance(@Valid @RequestBody AttendanceDTO dto) {
        AttendanceDTO response = attendanceService.createAttendance(dto);
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
    public List<AttendanceDTO> markClassAttendance(@Valid @RequestBody BulkAttendanceRequest request) {
        List<AttendanceDTO> responses = attendanceService.markClassAttendance(request);
        auditLogService.log(
            AuditAction.BULK_MARK,
            AuditEntityType.ATTENDANCE,
            request.getClassId(),
            "Class #" + request.getClassId(),
            "Marked attendance for " + responses.size() + " students on " + request.getAttendanceDate()
        );
        return responses;
    }

    @GetMapping
    @Operation(summary = "List attendance records")
    public Page<AttendanceDTO> getAllAttendance(Pageable pageable) {
        return attendanceService.getAllAttendance(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an attendance record by id")
    public AttendanceDTO getAttendanceById(@PathVariable Long id) {
        return attendanceService.getAttendanceById(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an attendance record")
    public AttendanceDTO updateAttendance(@PathVariable Long id, @Valid @RequestBody AttendanceDTO dto) {
        AttendanceDTO response = attendanceService.updateAttendance(id, dto);
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
    public void deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        auditLogService.log(AuditAction.DELETE, AuditEntityType.ATTENDANCE, id, "Attendance #" + id, "Deleted attendance record");
    }

    @GetMapping("/students/{studentId}")
    @Operation(summary = "List attendance records for a student")
    public Page<AttendanceDTO> getStudentAttendance(@PathVariable Long studentId, Pageable pageable) {
        return attendanceService.getStudentAttendance(studentId, pageable);
    }

    @GetMapping("/classes/{classId}")
    @Operation(summary = "List class attendance for a specific date")
    public Page<AttendanceDTO> getClassAttendanceByDate(
            @PathVariable Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable
    ) {
        return attendanceService.getClassAttendanceByDate(classId, date, pageable);
    }

    @GetMapping("/students/{studentId}/summary")
    @Operation(summary = "Get an attendance summary for a student within a date range")
    public AttendanceSummaryDTO getStudentAttendanceSummary(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return attendanceService.getStudentAttendanceSummary(studentId, from, to);
    }
}
