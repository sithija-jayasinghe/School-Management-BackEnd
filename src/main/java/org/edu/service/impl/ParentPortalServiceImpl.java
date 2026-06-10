package org.edu.service.impl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.AcademicReportDTO;
import org.edu.dto.DocumentDTO;
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
import org.edu.entity.Parent;
import org.edu.entity.ParentStudent;
import org.edu.entity.Student;
import org.edu.entity.Subject;
import org.edu.entity.Timetable;
import org.edu.exception.ResourceNotFoundException;
import org.edu.repository.ParentRepository;
import org.edu.repository.ParentStudentRepository;
import org.edu.repository.TimetableRepository;
import org.edu.service.AcademicReportService;
import org.edu.service.AttendanceService;
import org.edu.service.DocumentService;
import org.edu.service.LeaveRequestService;
import org.edu.service.NoticeService;
import org.edu.service.ParentPortalService;
import org.edu.service.StudentMarkService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParentPortalServiceImpl implements ParentPortalService {

    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final TimetableRepository timetableRepository;
    private final AcademicReportService academicReportService;
    private final AttendanceService attendanceService;
    private final DocumentService documentService;
    private final NoticeService noticeService;
    private final StudentMarkService studentMarkService;
    private final LeaveRequestService leaveRequestService;

    @Override
    public ParentPortalProfileDTO getProfile(Long authenticatedUserId) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        return toProfile(parent);
    }

    @Override
    public ParentPortalDashboardDTO getDashboard(Long authenticatedUserId) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        List<ParentPortalStudentSummaryDTO> students = parentStudentRepository
                .findActiveStudentLinksByParentId(parent.getId())
                .stream()
                .map(this::toStudentSummary)
                .toList();

        return new ParentPortalDashboardDTO(toProfile(parent), students.size(), students);
    }

    @Override
    public List<ParentPortalStudentSummaryDTO> getLinkedStudents(Long authenticatedUserId) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        return parentStudentRepository.findActiveStudentLinksByParentId(parent.getId())
                .stream()
                .map(this::toStudentSummary)
                .toList();
    }

    @Override
    public ParentPortalStudentDetailDTO getStudentDetail(Long authenticatedUserId, Long studentId) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        ParentStudent link = getAuthorizedStudentLink(parent.getId(), studentId);
        Student student = link.getStudent();

        return new ParentPortalStudentDetailDTO(
                student.getId(),
                student.getName(),
                student.getDateOfBirth(),
                student.getPhoneNumber(),
                student.isActive(),
                getClassId(student),
                getClassName(student),
                getClassTeacherName(student),
                link.getRelationshipType(),
                link.isPrimaryContact(),
                link.isEmergencyContact(),
                mapSubjects(student),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }

    @Override
    public List<ParentPortalSubjectDTO> getStudentSubjects(Long authenticatedUserId, Long studentId) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        ParentStudent link = getAuthorizedStudentLink(parent.getId(), studentId);
        return mapSubjects(link.getStudent());
    }

    @Override
    public List<ParentPortalTimetableEntryDTO> getStudentTimetable(Long authenticatedUserId, Long studentId) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        ParentStudent link = getAuthorizedStudentLink(parent.getId(), studentId);
        Student student = link.getStudent();

        if (student.getCurrentClass() == null) {
            return Collections.emptyList();
        }

        return timetableRepository.findPortalTimetableByClassId(student.getCurrentClass().getId())
                .stream()
                .map(this::toTimetableEntry)
                .toList();
    }

    @Override
    public List<ParentPortalAttendanceDTO> getStudentAttendance(
            Long authenticatedUserId,
            Long studentId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        getAuthorizedStudentLink(parent.getId(), studentId);
        return attendanceService.getPortalAttendance(studentId, fromDate, toDate);
    }

    @Override
    public List<ParentPortalNoticeDTO> getNotices(Long authenticatedUserId) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        List<Long> classIds = parentStudentRepository.findActiveStudentLinksByParentId(parent.getId())
                .stream()
                .map(ParentStudent::getStudent)
                .map(Student::getCurrentClass)
                .filter(java.util.Objects::nonNull)
                .map(org.edu.entity.Class::getId)
                .distinct()
                .toList();

        return noticeService.getParentPortalNotices(classIds);
    }

    @Override
    public List<ParentPortalResultDTO> getStudentResults(Long authenticatedUserId, Long studentId) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        getAuthorizedStudentLink(parent.getId(), studentId);
        return studentMarkService.getPortalResults(studentId);
    }

    @Override
    public org.springframework.data.domain.Page<ParentPortalDocumentDTO> getStudentDocuments(
            Long authenticatedUserId,
            Long studentId,
            Pageable pageable
    ) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        getAuthorizedStudentLink(parent.getId(), studentId);
        return documentService.getVisibleDocumentsByStudent(studentId, pageable)
                .map(this::toParentPortalDocument);
    }

    @Override
    public DocumentFileResponse downloadStudentDocument(Long authenticatedUserId, Long studentId, Long documentId) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        getAuthorizedStudentLink(parent.getId(), studentId);
        return documentService.downloadVisibleDocument(studentId, documentId);
    }

    @Override
    public Page<AcademicReportDTO> getStudentAcademicReports(
            Long authenticatedUserId,
            Long studentId,
            Pageable pageable
    ) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        getAuthorizedStudentLink(parent.getId(), studentId);
        return academicReportService.getPublishedStudentReports(studentId, pageable);
    }

    @Override
    public AcademicReportDTO getStudentAcademicReport(Long authenticatedUserId, Long studentId, Long reportId) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        getAuthorizedStudentLink(parent.getId(), studentId);
        AcademicReportDTO report = academicReportService.getPublishedReport(reportId);
        validateReportStudent(report, studentId);
        return report;
    }

    @Override
    public DocumentFileResponse downloadStudentReportCard(
            Long authenticatedUserId,
            Long studentId,
            Long reportId
    ) {
        getStudentAcademicReport(authenticatedUserId, studentId, reportId);
        return academicReportService.downloadPublishedReportCard(reportId);
    }

    @Override
    public LeaveRequestDTO createLeaveRequest(Long authenticatedUserId, ParentPortalLeaveRequestCreateDTO dto) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        getAuthorizedStudentLink(parent.getId(), dto.getStudentId());
        return leaveRequestService.createParentLeaveRequest(authenticatedUserId, dto);
    }

    @Override
    public Page<LeaveRequestDTO> getLeaveRequests(Long authenticatedUserId, Pageable pageable) {
        getActiveParentByUserId(authenticatedUserId);
        return leaveRequestService.getParentLeaveRequests(authenticatedUserId, pageable);
    }

    @Override
    public LeaveRequestDTO cancelLeaveRequest(Long authenticatedUserId, Long leaveRequestId, String remarks) {
        getActiveParentByUserId(authenticatedUserId);
        return leaveRequestService.cancelParentLeaveRequest(authenticatedUserId, leaveRequestId, remarks);
    }

    private Parent getActiveParentByUserId(Long authenticatedUserId) {
        return parentRepository.findByUser_IdAndActiveTrue(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Active parent profile not found for current user"));
    }

    private ParentStudent getAuthorizedStudentLink(Long parentId, Long studentId) {
        return parentStudentRepository.findActiveStudentLinkByParentIdAndStudentId(parentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found in current parent portal"));
    }

    private ParentPortalProfileDTO toProfile(Parent parent) {
        return new ParentPortalProfileDTO(
                parent.getId(),
                parent.getUser().getId(),
                parent.getName(),
                parent.getUser().getEmail(),
                parent.getPhoneNumber(),
                parent.getAddress(),
                parent.getOccupation(),
                parent.isActive(),
                parent.getCreatedAt(),
                parent.getUpdatedAt()
        );
    }

    private ParentPortalStudentSummaryDTO toStudentSummary(ParentStudent link) {
        Student student = link.getStudent();
        return new ParentPortalStudentSummaryDTO(
                student.getId(),
                student.getName(),
                student.getDateOfBirth(),
                student.getPhoneNumber(),
                getClassId(student),
                getClassName(student),
                link.getRelationshipType(),
                link.isPrimaryContact(),
                link.isEmergencyContact()
        );
    }

    private List<ParentPortalSubjectDTO> mapSubjects(Student student) {
        if (student.getCurrentClass() == null || student.getCurrentClass().getSubjects() == null) {
            return Collections.emptyList();
        }

        return student.getCurrentClass().getSubjects()
                .stream()
                .map(this::toSubject)
                .toList();
    }

    private ParentPortalSubjectDTO toSubject(Subject subject) {
        return new ParentPortalSubjectDTO(
                subject.getId(),
                subject.getCode(),
                subject.getName(),
                subject.getDescription()
        );
    }

    private ParentPortalTimetableEntryDTO toTimetableEntry(Timetable timetable) {
        return new ParentPortalTimetableEntryDTO(
                timetable.getId(),
                timetable.getDayOfWeek(),
                timetable.getStartTime(),
                timetable.getEndTime(),
                timetable.getRoomNumber(),
                timetable.getSubject().getId(),
                timetable.getSubject().getCode(),
                timetable.getSubject().getName(),
                timetable.getStaff().getId(),
                timetable.getStaff().getName()
        );
    }

    private ParentPortalDocumentDTO toParentPortalDocument(DocumentDTO document) {
        return new ParentPortalDocumentDTO(
                document.getId(),
                document.getStudentId(),
                document.getTitle(),
                document.getDescription(),
                document.getDocumentType(),
                document.getOriginalFileName(),
                document.getContentType(),
                document.getFileSize(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    private void validateReportStudent(AcademicReportDTO report, Long studentId) {
        if (!studentId.equals(report.getStudentId())) {
            throw new ResourceNotFoundException("Academic report not found in current parent portal");
        }
    }

    private Long getClassId(Student student) {
        return student.getCurrentClass() == null ? null : student.getCurrentClass().getId();
    }

    private String getClassName(Student student) {
        return student.getCurrentClass() == null ? null : student.getCurrentClass().getName();
    }

    private String getClassTeacherName(Student student) {
        if (student.getCurrentClass() == null || student.getCurrentClass().getClassTeacher() == null) {
            return null;
        }
        return student.getCurrentClass().getClassTeacher().getName();
    }
}
