package org.edu.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.edu.dto.AttendanceDTO;
import org.edu.dto.AttendanceSummaryDTO;
import org.edu.service.AttendanceService;
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
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public AttendanceDTO createAttendance(@Valid @RequestBody AttendanceDTO dto) {
        return attendanceService.createAttendance(dto);
    }

    @GetMapping
    public Page<AttendanceDTO> getAllAttendance(Pageable pageable) {
        return attendanceService.getAllAttendance(pageable);
    }

    @GetMapping("/{id}")
    public AttendanceDTO getAttendanceById(@PathVariable Long id) {
        return attendanceService.getAttendanceById(id);
    }

    @PatchMapping("/{id}")
    public AttendanceDTO updateAttendance(@PathVariable Long id, @Valid @RequestBody AttendanceDTO dto) {
        return attendanceService.updateAttendance(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
    }

    @GetMapping("/students/{studentId}")
    public Page<AttendanceDTO> getStudentAttendance(@PathVariable Long studentId, Pageable pageable) {
        return attendanceService.getStudentAttendance(studentId, pageable);
    }

    @GetMapping("/classes/{classId}")
    public Page<AttendanceDTO> getClassAttendanceByDate(
            @PathVariable Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable
    ) {
        return attendanceService.getClassAttendanceByDate(classId, date, pageable);
    }

    @GetMapping("/students/{studentId}/summary")
    public AttendanceSummaryDTO getStudentAttendanceSummary(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return attendanceService.getStudentAttendanceSummary(studentId, from, to);
    }
}
