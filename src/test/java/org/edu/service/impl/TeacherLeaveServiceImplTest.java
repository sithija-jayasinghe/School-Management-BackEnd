package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.edu.dto.teacherleave.TeacherLeaveCoverageRequest;
import org.edu.dto.teacherleave.TeacherLeaveReviewRequest;
import org.edu.dto.teacherleave.TeacherLeaveSaveRequest;
import org.edu.entity.Staff;
import org.edu.entity.Subject;
import org.edu.entity.TeacherLeaveRequest;
import org.edu.entity.TeacherLeaveSession;
import org.edu.entity.Timetable;
import org.edu.entity.User;
import org.edu.exception.ResourceNotFoundException;
import org.edu.repository.StaffRepository;
import org.edu.repository.TeacherLeaveRequestRepository;
import org.edu.repository.TeacherLeaveSessionRepository;
import org.edu.repository.TeachingAssignmentRepository;
import org.edu.repository.TimetableRepository;
import org.edu.repository.UserRepository;
import org.edu.service.AuditLogService;
import org.edu.util.Role;
import org.edu.util.TeacherLeaveCoverageStatus;
import org.edu.util.TeacherLeaveDuration;
import org.edu.util.TeacherLeaveStatus;
import org.edu.util.TeacherLeaveType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeacherLeaveServiceImplTest {

    @Mock private TeacherLeaveRequestRepository requestRepository;
    @Mock private TeacherLeaveSessionRepository sessionRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private UserRepository userRepository;
    @Mock private TimetableRepository timetableRepository;
    @Mock private TeachingAssignmentRepository teachingAssignmentRepository;
    @Mock private AuditLogService auditLogService;

    private TeacherLeaveServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TeacherLeaveServiceImpl(
                requestRepository,
                sessionRepository,
                staffRepository,
                userRepository,
                timetableRepository,
                teachingAssignmentRepository,
                auditLogService
        );
    }

    @Test
    void shouldSubmitLeaveAndCreateAffectedSessionsFromTeacherTimetable() {
        Staff teacher = teacher(10L, 100L, "Anuradha Jayalath");
        TeacherLeaveSaveRequest input = fullDayRequest(LocalDate.of(2026, 7, 20));
        Timetable timetable = timetable(teacher, DayOfWeek.MONDAY);

        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(teacher));
        when(requestRepository.existsOverlappingRequest(any(), any(), any(), any(), any())).thenReturn(false);
        when(timetableRepository.findTeacherPortalScheduleByStaffId(10L)).thenReturn(List.of(timetable));
        when(requestRepository.saveAndFlush(any(TeacherLeaveRequest.class))).thenAnswer(invocation -> {
            TeacherLeaveRequest saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.getAffectedSessions().get(0).setId(2L);
            return saved;
        });

        var result = service.submit(100L, input);

        assertEquals(TeacherLeaveStatus.PENDING, result.getStatus());
        assertEquals(1, result.getAffectedSessionCount());
        assertEquals("Grade 10 A", result.getAffectedSessions().get(0).getClassName());
        verify(requestRepository).saveAndFlush(any(TeacherLeaveRequest.class));
    }

    @Test
    void shouldHideAnotherTeachersRequestFromTeacherPortal() {
        Staff currentTeacher = teacher(10L, 100L, "Anuradha Jayalath");
        TeacherLeaveRequest otherRequest = leaveRequest(teacher(20L, 200L, "Nimali Perera"));
        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(currentTeacher));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(otherRequest));

        assertThrows(ResourceNotFoundException.class, () -> service.getOwnRequest(100L, 1L));
    }

    @Test
    void shouldBlockApprovalUntilEveryAffectedSessionHasCoverageDecision() {
        TeacherLeaveRequest request = leaveRequest(teacher(10L, 100L, "Anuradha Jayalath"));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(sessionRepository.countByLeaveRequestIdAndCoverageStatus(1L, TeacherLeaveCoverageStatus.UNASSIGNED))
                .thenReturn(1L);

        assertThrows(
                IllegalStateException.class,
                () -> service.approve(999L, 1L, reviewRequest())
        );
    }

    @Test
    void shouldRequireReasonWhenCoverageIsNotRequired() {
        TeacherLeaveSession session = session(leaveRequest(teacher(10L, 100L, "Anuradha Jayalath")));
        TeacherLeaveCoverageRequest input = new TeacherLeaveCoverageRequest();
        input.setCoverageStatus(TeacherLeaveCoverageStatus.NOT_REQUIRED);
        when(sessionRepository.findById(2L)).thenReturn(Optional.of(session));

        assertThrows(IllegalArgumentException.class, () -> service.updateCoverage(2L, input));
    }

    private TeacherLeaveSaveRequest fullDayRequest(LocalDate date) {
        TeacherLeaveSaveRequest input = new TeacherLeaveSaveRequest();
        input.setLeaveType(TeacherLeaveType.MEDICAL);
        input.setDurationType(TeacherLeaveDuration.FULL_DAY);
        input.setStartDate(date);
        input.setEndDate(date);
        input.setReason("Medical appointment");
        return input;
    }

    private TeacherLeaveReviewRequest reviewRequest() {
        TeacherLeaveReviewRequest request = new TeacherLeaveReviewRequest();
        request.setReviewerRemarks("Reviewed by administration");
        return request;
    }

    private Staff teacher(Long staffId, Long userId, String name) {
        User user = new User();
        user.setId(userId);
        user.setName(name);
        user.setEmail(name.replace(" ", ".").toLowerCase() + "@school.lk");
        user.setRole(Role.TEACHER);
        user.setActive(true);

        Staff staff = new Staff();
        staff.setId(staffId);
        staff.setUser(user);
        staff.setStaffId("T" + staffId);
        staff.setName(name);
        staff.setDesignation("Teacher");
        staff.setActive(true);
        return staff;
    }

    private Timetable timetable(Staff teacher, DayOfWeek day) {
        org.edu.entity.Class studentClass = new org.edu.entity.Class();
        studentClass.setId(30L);
        studentClass.setName("Grade 10 A");

        Subject subject = new Subject();
        subject.setId(40L);
        subject.setName("Mathematics");

        Timetable timetable = new Timetable();
        timetable.setId(50L);
        timetable.setStaff(teacher);
        timetable.setStudentClass(studentClass);
        timetable.setSubject(subject);
        timetable.setDayOfWeek(day);
        timetable.setStartTime(LocalTime.of(8, 0));
        timetable.setEndTime(LocalTime.of(8, 40));
        timetable.setRoomNumber("10-A");
        return timetable;
    }

    private TeacherLeaveRequest leaveRequest(Staff teacher) {
        TeacherLeaveRequest request = new TeacherLeaveRequest();
        request.setId(1L);
        request.setTeacher(teacher);
        request.setLeaveType(TeacherLeaveType.CASUAL);
        request.setDurationType(TeacherLeaveDuration.FULL_DAY);
        request.setStartDate(LocalDate.of(2026, 7, 20));
        request.setEndDate(LocalDate.of(2026, 7, 20));
        request.setReason("Personal matter");
        request.setStatus(TeacherLeaveStatus.PENDING);
        return request;
    }

    private TeacherLeaveSession session(TeacherLeaveRequest request) {
        Timetable timetable = timetable(request.getTeacher(), DayOfWeek.MONDAY);
        TeacherLeaveSession session = new TeacherLeaveSession();
        session.setId(2L);
        session.setLeaveRequest(request);
        session.setTimetable(timetable);
        session.setSessionDate(LocalDate.of(2026, 7, 20));
        session.setStartTime(timetable.getStartTime());
        session.setEndTime(timetable.getEndTime());
        session.setClassIdSnapshot(30L);
        session.setClassNameSnapshot("Grade 10 A");
        session.setSubjectIdSnapshot(40L);
        session.setSubjectNameSnapshot("Mathematics");
        return session;
    }
}
