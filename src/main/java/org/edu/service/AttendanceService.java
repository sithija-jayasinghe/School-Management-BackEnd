package org.edu.service;

import java.time.LocalDate;
import java.util.List;
import org.edu.dto.AttendanceDTO;
import org.edu.dto.AttendanceSummaryDTO;
import org.edu.dto.parentportal.ParentPortalAttendanceDTO;
import org.edu.dto.request.BulkAttendanceRequest;
import org.edu.util.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AttendanceService {

    AttendanceDTO createAttendance(Long authenticatedUserId, AttendanceDTO dto);

    List<AttendanceDTO> markClassAttendance(Long authenticatedUserId, BulkAttendanceRequest request);

    AttendanceDTO updateAttendance(Long authenticatedUserId, Long id, AttendanceDTO dto);

    void deleteAttendance(Long authenticatedUserId, Long id);

    AttendanceDTO getAttendanceById(Long authenticatedUserId, Long id);

    Page<AttendanceDTO> getAllAttendance(Long authenticatedUserId, Pageable pageable);

    Page<AttendanceDTO> getStudentAttendance(Long authenticatedUserId, Long studentId, Pageable pageable);

    Page<AttendanceDTO> getClassAttendanceByDate(Long authenticatedUserId, Long classId, LocalDate attendanceDate, Pageable pageable);

    Page<AttendanceDTO> filterAttendance(Long authenticatedUserId, Long classId, Long studentId, Long subjectId, Long markedByStaffId, AttendanceStatus status, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    AttendanceSummaryDTO getStudentAttendanceSummary(Long authenticatedUserId, Long studentId, LocalDate fromDate, LocalDate toDate);

    List<ParentPortalAttendanceDTO> getPortalAttendance(Long studentId, LocalDate fromDate, LocalDate toDate);
}
