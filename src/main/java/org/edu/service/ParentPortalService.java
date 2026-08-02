package org.edu.service;

import java.time.LocalDate;
import java.util.List;
import org.edu.dto.AcademicReportDTO;
import org.edu.dto.DocumentFileResponse;
import org.edu.dto.LeaveRequestDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ParentPortalService {

    ParentPortalProfileDTO getProfile(Long authenticatedUserId);

    ParentPortalDashboardDTO getDashboard(Long authenticatedUserId);

    List<ParentPortalStudentSummaryDTO> getLinkedStudents(Long authenticatedUserId);

    ParentPortalStudentDetailDTO getStudentDetail(Long authenticatedUserId, Long studentId);

    List<ParentPortalSubjectDTO> getStudentSubjects(Long authenticatedUserId, Long studentId);

    List<ParentPortalTimetableEntryDTO> getStudentTimetable(Long authenticatedUserId, Long studentId);

    List<ParentPortalAttendanceDTO> getStudentAttendance(Long authenticatedUserId, Long studentId, LocalDate fromDate, LocalDate toDate);

    List<ParentPortalNoticeDTO> getNotices(Long authenticatedUserId);

    List<ParentPortalResultDTO> getStudentResults(Long authenticatedUserId, Long studentId);

    Page<ParentPortalDocumentDTO> getStudentDocuments(Long authenticatedUserId, Long studentId, Pageable pageable);

    DocumentFileResponse downloadStudentDocument(Long authenticatedUserId, Long studentId, Long documentId);

    Page<AcademicReportDTO> getStudentAcademicReports(
            Long authenticatedUserId,
            Long studentId,
            Pageable pageable
    );

    AcademicReportDTO getStudentAcademicReport(Long authenticatedUserId, Long studentId, Long reportId);

    DocumentFileResponse downloadStudentReportCard(Long authenticatedUserId, Long studentId, Long reportId);

    LeaveRequestDTO createLeaveRequest(Long authenticatedUserId, ParentPortalLeaveRequestCreateDTO dto, MultipartFile leaveLetterFile);

    Page<LeaveRequestDTO> getLeaveRequests(Long authenticatedUserId, Pageable pageable);

    LeaveRequestDTO cancelLeaveRequest(Long authenticatedUserId, Long leaveRequestId, String remarks);

    void deleteLeaveRequest(Long authenticatedUserId, Long leaveRequestId);
}
