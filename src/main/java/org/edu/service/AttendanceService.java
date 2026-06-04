package org.edu.service;

import java.time.LocalDate;
import java.util.List;
import org.edu.dto.AttendanceDTO;
import org.edu.dto.AttendanceSummaryDTO;
import org.edu.dto.parentportal.ParentPortalAttendanceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AttendanceService {

    AttendanceDTO createAttendance(AttendanceDTO dto);

    AttendanceDTO updateAttendance(Long id, AttendanceDTO dto);

    void deleteAttendance(Long id);

    AttendanceDTO getAttendanceById(Long id);

    Page<AttendanceDTO> getAllAttendance(Pageable pageable);

    Page<AttendanceDTO> getStudentAttendance(Long studentId, Pageable pageable);

    Page<AttendanceDTO> getClassAttendanceByDate(Long classId, LocalDate attendanceDate, Pageable pageable);

    AttendanceSummaryDTO getStudentAttendanceSummary(Long studentId, LocalDate fromDate, LocalDate toDate);

    List<ParentPortalAttendanceDTO> getPortalAttendance(Long studentId, LocalDate fromDate, LocalDate toDate);
}
