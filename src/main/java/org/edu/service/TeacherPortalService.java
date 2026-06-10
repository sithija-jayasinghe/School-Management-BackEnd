package org.edu.service;

import java.time.LocalDate;
import java.util.List;
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
import org.edu.dto.request.AcademicReportUpdateRequest;
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

    Page<TeacherPortalDocumentDTO> getStudentDocuments(Long authenticatedUserId, Long studentId, Pageable pageable);

    TeacherPortalDocumentDTO uploadStudentDocument(
            Long authenticatedUserId,
            Long studentId,
            TeacherPortalDocumentCreateRequest request,
            org.springframework.web.multipart.MultipartFile file
    );

    DocumentFileResponse downloadStudentDocument(Long authenticatedUserId, Long studentId, Long documentId);

    AcademicReportDTO generateStudentAcademicReport(
            Long authenticatedUserId,
            Long studentId,
            TeacherPortalAcademicReportGenerateRequest request
    );

    AcademicReportDTO regenerateAcademicReport(Long authenticatedUserId, Long reportId);

    AcademicReportDTO updateAcademicReport(
            Long authenticatedUserId,
            Long reportId,
            AcademicReportUpdateRequest request
    );

    AcademicReportDTO publishAcademicReport(Long authenticatedUserId, Long reportId);

    Page<AcademicReportDTO> getStudentAcademicReports(
            Long authenticatedUserId,
            Long studentId,
            Pageable pageable
    );

    Page<AcademicReportDTO> getClassAcademicReports(
            Long authenticatedUserId,
            Long classId,
            Long academicTermId,
            Pageable pageable
    );

    DocumentFileResponse downloadAcademicReportCard(Long authenticatedUserId, Long reportId);

    List<AttendanceDTO> markClassAttendance(Long authenticatedUserId, Long classId, TeacherPortalBulkAttendanceRequest request);

    Page<LeaveRequestDTO> getLeaveRequests(Long authenticatedUserId, LeaveRequestStatus status, Pageable pageable);

    LeaveRequestDTO approveLeaveRequest(Long authenticatedUserId, Long leaveRequestId, TeacherPortalLeaveReviewRequest request);

    LeaveRequestDTO rejectLeaveRequest(Long authenticatedUserId, Long leaveRequestId, TeacherPortalLeaveReviewRequest request);

    LeaveRequestDTO applyLeaveToAttendance(Long authenticatedUserId, Long leaveRequestId);
}
