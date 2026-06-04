package org.edu.service.impl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.parentportal.ParentPortalAttendanceDTO;
import org.edu.dto.parentportal.ParentPortalDashboardDTO;
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
import org.edu.service.AttendanceService;
import org.edu.service.ParentPortalService;
import org.edu.service.StudentMarkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParentPortalServiceImpl implements ParentPortalService {

    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final TimetableRepository timetableRepository;
    private final AttendanceService attendanceService;
    private final StudentMarkService studentMarkService;

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
    public List<ParentPortalResultDTO> getStudentResults(Long authenticatedUserId, Long studentId) {
        Parent parent = getActiveParentByUserId(authenticatedUserId);
        getAuthorizedStudentLink(parent.getId(), studentId);
        return studentMarkService.getPortalResults(studentId);
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