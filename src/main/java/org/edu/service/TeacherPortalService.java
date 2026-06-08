package org.edu.service;

import java.time.LocalDate;
import java.util.List;
import org.edu.dto.AttendanceDTO;
import org.edu.dto.AttendanceSummaryDTO;
import org.edu.dto.LeaveRequestDTO;
import org.edu.dto.teacherportal.TeacherPortalClassSummaryDTO;
import org.edu.dto.teacherportal.TeacherPortalBulkAttendanceRequest;
import org.edu.dto.teacherportal.TeacherPortalDashboardDTO;
import org.edu.dto.teacherportal.TeacherPortalExamDTO;
import org.edu.dto.teacherportal.TeacherPortalLeaveReviewRequest;
import org.edu.dto.teacherportal.TeacherPortalProfileDTO;
import org.edu.dto.teacherportal.TeacherPortalStudentDTO;
import org.edu.dto.teacherportal.TeacherPortalSubjectDTO;
import org.edu.dto.teacherportal.TeacherPortalTimetableEntryDTO;
import org.edu.util.LeaveRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeacherPortalService {

    TeacherPortalProfileDTO getProfile(Long authenticatedUserId);

    TeacherPortalDashboardDTO getDashboard(Long authenticatedUserId);

    List<TeacherPortalClassSummaryDTO> getAssignedClasses(Long authenticatedUserId);

    List<TeacherPortalTimetableEntryDTO> getSchedule(Long authenticatedUserId);

    List<TeacherPortalSubjectDTO> getSubjects(Long authenticatedUserId);

    List<TeacherPortalStudentDTO> getClassStudents(Long authenticatedUserId, Long classId);

    List<TeacherPortalExamDTO> getExams(Long authenticatedUserId);

    Page<AttendanceDTO> getClassAttendanceByDate(Long authenticatedUserId, Long classId, LocalDate attendanceDate, Pageable pageable);

    Page<AttendanceDTO> getStudentAttendance(Long authenticatedUserId, Long studentId, Pageable pageable);

    AttendanceSummaryDTO getStudentAttendanceSummary(Long authenticatedUserId, Long studentId, LocalDate fromDate, LocalDate toDate);

    List<AttendanceDTO> markClassAttendance(Long authenticatedUserId, Long classId, TeacherPortalBulkAttendanceRequest request);

    Page<LeaveRequestDTO> getLeaveRequests(Long authenticatedUserId, LeaveRequestStatus status, Pageable pageable);

    LeaveRequestDTO approveLeaveRequest(Long authenticatedUserId, Long leaveRequestId, TeacherPortalLeaveReviewRequest request);

    LeaveRequestDTO rejectLeaveRequest(Long authenticatedUserId, Long leaveRequestId, TeacherPortalLeaveReviewRequest request);
}
