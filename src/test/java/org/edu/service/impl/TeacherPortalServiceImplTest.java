package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.edu.dto.AttendanceDTO;
import org.edu.dto.AttendanceSummaryDTO;
import org.edu.dto.LeaveRequestDTO;
import org.edu.dto.request.BulkAttendanceStudentRequest;
import org.edu.dto.teacherportal.TeacherPortalDashboardDTO;
import org.edu.dto.teacherportal.TeacherPortalBulkAttendanceRequest;
import org.edu.dto.teacherportal.TeacherPortalExamDTO;
import org.edu.dto.teacherportal.TeacherPortalLeaveReviewRequest;
import org.edu.dto.teacherportal.TeacherPortalProfileDTO;
import org.edu.dto.teacherportal.TeacherPortalStudentDTO;
import org.edu.dto.teacherportal.TeacherPortalSubjectDTO;
import org.edu.dto.teacherportal.TeacherPortalTimetableEntryDTO;
import org.edu.entity.Exam;
import org.edu.entity.Staff;
import org.edu.entity.Student;
import org.edu.entity.AcademicTerm;
import org.edu.entity.AcademicYear;
import org.edu.entity.Subject;
import org.edu.entity.Timetable;
import org.edu.entity.User;
import org.edu.exception.ResourceNotFoundException;
import org.edu.repository.ClassRepository;
import org.edu.repository.ExamRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.TimetableRepository;
import org.edu.service.AttendanceService;
import org.edu.service.LeaveRequestService;
import org.edu.util.AttendanceStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.edu.util.ExamType;
import org.edu.util.LeaveRequestStatus;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TeacherPortalServiceImplTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private TimetableRepository timetableRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private LeaveRequestService leaveRequestService;

    @InjectMocks
    private TeacherPortalServiceImpl teacherPortalService;

    @Test
    void shouldReturnProfileForAuthenticatedTeacher() {
        Staff staff = teacherWithUser(10L, 100L);

        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(staff));

        TeacherPortalProfileDTO profile = teacherPortalService.getProfile(100L);

        assertEquals(10L, profile.getStaffId());
        assertEquals("T-001", profile.getStaffCode());
        assertEquals("teacher@example.com", profile.getEmail());
    }

    @Test
    void shouldRejectTeacherPortalAccessWithoutActiveTeacherProfile() {
        when(staffRepository.findByUser_IdAndActiveTrue(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherPortalService.getDashboard(999L));
    }

    @Test
    void shouldBuildDashboardWithAssignedClassesAndSchedule() {
        Staff staff = teacherWithUser(10L, 100L);
        org.edu.entity.Class grade10A = studentClass(30L, "Grade 10A", 32, 3, staff);
        org.edu.entity.Class grade11B = studentClass(31L, "Grade 11B", 28, 2, staff);
        Timetable timetable = timetable(grade10A, subject(40L, "SCI", "Science"), staff);

        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(staff));
        when(classRepository.findByClassTeacherIdAndActiveTrueOrderByNameAsc(10L))
                .thenReturn(List.of(grade10A, grade11B));
        when(timetableRepository.findTeacherPortalScheduleByStaffId(10L)).thenReturn(List.of(timetable));

        TeacherPortalDashboardDTO dashboard = teacherPortalService.getDashboard(100L);

        assertEquals(2, dashboard.getAssignedClassCount());
        assertEquals(1, dashboard.getWeeklySessionCount());
        assertEquals("Grade 10A", dashboard.getAssignedClasses().get(0).getClassName());
        assertEquals(32, dashboard.getAssignedClasses().get(0).getStudentCount());
        assertEquals("Science", dashboard.getSchedule().get(0).getSubjectName());
    }

    @Test
    void shouldReturnTeacherSchedule() {
        Staff staff = teacherWithUser(10L, 100L);
        org.edu.entity.Class grade10A = studentClass(30L, "Grade 10A", 32, 3, staff);
        Timetable timetable = timetable(grade10A, subject(40L, "MATH", "Mathematics"), staff);

        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(staff));
        when(timetableRepository.findTeacherPortalScheduleByStaffId(10L)).thenReturn(List.of(timetable));

        List<TeacherPortalTimetableEntryDTO> schedule = teacherPortalService.getSchedule(100L);

        assertEquals(1, schedule.size());
        assertEquals(DayOfWeek.MONDAY, schedule.get(0).getDayOfWeek());
        assertEquals("Grade 10A", schedule.get(0).getClassName());
        assertEquals("Mathematics", schedule.get(0).getSubjectName());
        verify(timetableRepository).findTeacherPortalScheduleByStaffId(10L);
    }

    @Test
    void shouldReturnTeacherSubjectsGroupedFromSchedule() {
        Staff staff = teacherWithUser(10L, 100L);
        Subject math = subject(40L, "MATH", "Mathematics");
        math.setDescription("Math subject");
        org.edu.entity.Class grade10A = studentClass(30L, "Grade 10A", 32, 3, staff);
        org.edu.entity.Class grade11B = studentClass(31L, "Grade 11B", 28, 2, staff);

        Timetable sessionOne = timetable(grade10A, math, staff);
        Timetable sessionTwo = timetable(grade11B, math, staff);

        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(staff));
        when(timetableRepository.findTeacherPortalScheduleByStaffId(10L)).thenReturn(List.of(sessionOne, sessionTwo));

        List<TeacherPortalSubjectDTO> subjects = teacherPortalService.getSubjects(100L);

        assertEquals(1, subjects.size());
        assertEquals("Mathematics", subjects.get(0).getSubjectName());
        assertEquals(2, subjects.get(0).getAssignedClassCount());
        assertEquals(2, subjects.get(0).getWeeklySessionCount());
    }

    @Test
    void shouldReturnStudentsForTeacherAccessibleClass() {
        Staff staff = teacherWithUser(10L, 100L);
        Student studentOne = student(20L, "Amal", 30L, "Grade 10A");
        Student studentTwo = student(21L, "Bimal", 30L, "Grade 10A");

        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(staff));
        when(classRepository.existsByIdAndClassTeacherIdAndActiveTrue(30L, 10L)).thenReturn(false);
        when(timetableRepository.existsByStaffIdAndStudentClassId(10L, 30L)).thenReturn(true);
        when(studentRepository.findByCurrentClassIdAndActiveTrueOrderByNameAsc(30L))
                .thenReturn(List.of(studentOne, studentTwo));

        List<TeacherPortalStudentDTO> students = teacherPortalService.getClassStudents(100L, 30L);

        assertEquals(2, students.size());
        assertEquals("Amal", students.get(0).getName());
        assertEquals("Grade 10A", students.get(0).getClassName());
    }

    @Test
    void shouldRejectStudentsForInaccessibleClass() {
        Staff staff = teacherWithUser(10L, 100L);

        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(staff));
        when(classRepository.existsByIdAndClassTeacherIdAndActiveTrue(99L, 10L)).thenReturn(false);
        when(timetableRepository.existsByStaffIdAndStudentClassId(10L, 99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> teacherPortalService.getClassStudents(100L, 99L));
    }

    @Test
    void shouldReturnTeacherRelevantExams() {
        Staff staff = teacherWithUser(10L, 100L);
        org.edu.entity.Class grade10A = studentClass(30L, "Grade 10A", 32, 3, staff);
        Subject science = subject(40L, "SCI", "Science");
        Exam exam = exam(80L, "Term 1 Science Test", grade10A, science);

        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(staff));
        when(examRepository.findTeacherPortalExamsByStaffId(10L)).thenReturn(List.of(exam));

        List<TeacherPortalExamDTO> exams = teacherPortalService.getExams(100L);

        assertEquals(1, exams.size());
        assertEquals("Term 1 Science Test", exams.get(0).getExamName());
        assertEquals("Science", exams.get(0).getSubjectName());
        assertEquals("Grade 10A", exams.get(0).getClassName());
    }

    @Test
    void shouldReturnClassAttendanceForTeacherAccessibleClass() {
        Staff staff = teacherWithUser(10L, 100L);
        AttendanceDTO attendance = new AttendanceDTO();
        attendance.setId(1L);
        attendance.setClassId(30L);
        attendance.setAttendanceDate(LocalDate.of(2026, 1, 5));
        attendance.setStatus(AttendanceStatus.PRESENT);

        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(staff));
        when(classRepository.existsByIdAndClassTeacherIdAndActiveTrue(30L, 10L)).thenReturn(true);
        when(attendanceService.getClassAttendanceByDate(30L, LocalDate.of(2026, 1, 5), Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(attendance)));

        var page = teacherPortalService.getClassAttendanceByDate(
                100L,
                30L,
                LocalDate.of(2026, 1, 5),
                Pageable.unpaged()
        );

        assertEquals(1, page.getTotalElements());
        assertEquals(AttendanceStatus.PRESENT, page.getContent().get(0).getStatus());
    }

    @Test
    void shouldReturnStudentAttendanceSummaryForAccessibleStudent() {
        Staff staff = teacherWithUser(10L, 100L);
        Student student = student(20L, "Amal", 30L, "Grade 10A");
        AttendanceSummaryDTO summary = new AttendanceSummaryDTO(
                20L,
                "Amal",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                10,
                8,
                1,
                1,
                0,
                90.0
        );

        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(staff));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));
        when(classRepository.existsByIdAndClassTeacherIdAndActiveTrue(30L, 10L)).thenReturn(false);
        when(timetableRepository.existsByStaffIdAndStudentClassId(10L, 30L)).thenReturn(true);
        when(attendanceService.getStudentAttendanceSummary(
                20L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        )).thenReturn(summary);

        AttendanceSummaryDTO result = teacherPortalService.getStudentAttendanceSummary(
                100L,
                20L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );

        assertEquals(90.0, result.getAttendancePercentage());
        assertEquals("Amal", result.getStudentName());
    }

    @Test
    void shouldMarkClassAttendanceUsingAuthenticatedTeacher() {
        Staff staff = teacherWithUser(10L, 100L);
        TeacherPortalBulkAttendanceRequest request = new TeacherPortalBulkAttendanceRequest(
                null,
                60L,
                LocalDate.of(2026, 1, 5),
                List.of(new BulkAttendanceStudentRequest(20L, AttendanceStatus.PRESENT, "On time"))
        );

        AttendanceDTO savedAttendance = new AttendanceDTO();
        savedAttendance.setId(1L);
        savedAttendance.setMarkedByStaffId(10L);

        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(staff));
        when(timetableRepository.existsByIdAndStaffIdAndStudentClassId(60L, 10L, 30L)).thenReturn(true);
        when(attendanceService.markClassAttendance(any())).thenReturn(List.of(savedAttendance));

        List<AttendanceDTO> saved = teacherPortalService.markClassAttendance(100L, 30L, request);

        assertEquals(1, saved.size());
        verify(attendanceService).markClassAttendance(argThat(bulkRequest ->
                bulkRequest.getClassId().equals(30L)
                        && bulkRequest.getMarkedByStaffId().equals(10L)
                        && bulkRequest.getTimetableId().equals(60L)
                        && bulkRequest.getStudents().size() == 1
        ));
    }

    @Test
    void shouldRejectDailyAttendanceMarkingForNonClassTeacher() {
        Staff staff = teacherWithUser(10L, 100L);
        TeacherPortalBulkAttendanceRequest request = new TeacherPortalBulkAttendanceRequest(
                null,
                null,
                LocalDate.of(2026, 1, 5),
                List.of(new BulkAttendanceStudentRequest(20L, AttendanceStatus.PRESENT, null))
        );

        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(staff));
        when(classRepository.existsByIdAndClassTeacherIdAndActiveTrue(30L, 10L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> teacherPortalService.markClassAttendance(100L, 30L, request));
    }

    @Test
    void shouldReturnTeacherAccessibleLeaveRequests() {
        Staff staff = teacherWithUser(10L, 100L);
        LeaveRequestDTO leaveRequest = new LeaveRequestDTO();
        leaveRequest.setId(1L);
        leaveRequest.setStudentId(20L);
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);

        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(staff));
        when(leaveRequestService.getTeacherLeaveRequests(100L, LeaveRequestStatus.PENDING, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(leaveRequest)));

        var page = teacherPortalService.getLeaveRequests(100L, LeaveRequestStatus.PENDING, Pageable.unpaged());

        assertEquals(1, page.getTotalElements());
        assertEquals(LeaveRequestStatus.PENDING, page.getContent().get(0).getStatus());
    }

    @Test
    void shouldApproveTeacherAccessibleLeaveRequest() {
        Staff staff = teacherWithUser(10L, 100L);
        LeaveRequestDTO leaveRequest = new LeaveRequestDTO();
        leaveRequest.setId(1L);
        leaveRequest.setReviewedByStaffId(10L);
        leaveRequest.setStatus(LeaveRequestStatus.APPROVED);

        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(staff));
        when(leaveRequestService.approveTeacherLeaveRequest(100L, 1L, "Approved"))
                .thenReturn(leaveRequest);

        LeaveRequestDTO approved = teacherPortalService.approveLeaveRequest(
                100L,
                1L,
                new TeacherPortalLeaveReviewRequest("Approved")
        );

        assertEquals(LeaveRequestStatus.APPROVED, approved.getStatus());
        assertEquals(10L, approved.getReviewedByStaffId());
    }

    @Test
    void shouldRejectTeacherAccessibleLeaveRequest() {
        Staff staff = teacherWithUser(10L, 100L);
        LeaveRequestDTO leaveRequest = new LeaveRequestDTO();
        leaveRequest.setId(2L);
        leaveRequest.setReviewedByStaffId(10L);
        leaveRequest.setStatus(LeaveRequestStatus.REJECTED);

        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(staff));
        when(leaveRequestService.rejectTeacherLeaveRequest(100L, 2L, "Insufficient detail"))
                .thenReturn(leaveRequest);

        LeaveRequestDTO rejected = teacherPortalService.rejectLeaveRequest(
                100L,
                2L,
                new TeacherPortalLeaveReviewRequest("Insufficient detail")
        );

        assertEquals(LeaveRequestStatus.REJECTED, rejected.getStatus());
        assertEquals(10L, rejected.getReviewedByStaffId());
    }

    private Staff teacherWithUser(Long staffPk, Long userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail("teacher@example.com");

        Staff staff = new Staff();
        staff.setId(staffPk);
        staff.setUser(user);
        staff.setStaffId("T-001");
        staff.setName("Nimal Perera");
        staff.setPhoneNumber("0771234567");
        staff.setDesignation("Teacher");
        staff.setActive(true);
        return staff;
    }

    private org.edu.entity.Class studentClass(
            Long classId,
            String name,
            int studentCount,
            int subjectCount,
            Staff classTeacher
    ) {
        org.edu.entity.Class studentClass = new org.edu.entity.Class();
        studentClass.setId(classId);
        studentClass.setName(name);
        studentClass.setClassTeacher(classTeacher);
        studentClass.setStudents(java.util.stream.IntStream.range(0, studentCount)
                .mapToObj(index -> new org.edu.entity.Student())
                .toList());
        studentClass.setSubjects(java.util.stream.IntStream.range(0, subjectCount)
                .mapToObj(index -> new Subject())
                .toList());
        return studentClass;
    }

    private Subject subject(Long subjectId, String code, String name) {
        Subject subject = new Subject();
        subject.setId(subjectId);
        subject.setCode(code);
        subject.setName(name);
        subject.setDescription(name + " subject");
        return subject;
    }

    private Student student(Long studentId, String name, Long classId, String className) {
        org.edu.entity.Class studentClass = new org.edu.entity.Class();
        studentClass.setId(classId);
        studentClass.setName(className);

        Student student = new Student();
        student.setId(studentId);
        student.setName(name);
        student.setDateOfBirth(LocalDate.of(2010, 1, 1));
        student.setPhoneNumber("0771234567");
        student.setCurrentClass(studentClass);
        student.setActive(true);
        return student;
    }

    private Exam exam(Long examId, String name, org.edu.entity.Class studentClass, Subject subject) {
        AcademicYear academicYear = new AcademicYear();
        academicYear.setId(70L);
        academicYear.setName("2026");

        AcademicTerm academicTerm = new AcademicTerm();
        academicTerm.setId(71L);
        academicTerm.setName("Term 1");
        academicTerm.setAcademicYear(academicYear);

        Exam exam = new Exam();
        exam.setId(examId);
        exam.setName(name);
        exam.setType(ExamType.TERM_TEST);
        exam.setExamDate(LocalDate.of(2026, 3, 15));
        exam.setAcademicYear(academicYear);
        exam.setAcademicTerm(academicTerm);
        exam.setStudentClass(studentClass);
        exam.setSubject(subject);
        exam.setMaxMarks(BigDecimal.valueOf(100));
        exam.setPassMarks(BigDecimal.valueOf(40));
        exam.setActive(true);
        return exam;
    }

    private Timetable timetable(org.edu.entity.Class studentClass, Subject subject, Staff staff) {
        Timetable timetable = new Timetable();
        timetable.setId(60L);
        timetable.setStudentClass(studentClass);
        timetable.setSubject(subject);
        timetable.setStaff(staff);
        timetable.setDayOfWeek(DayOfWeek.MONDAY);
        timetable.setStartTime(LocalTime.of(8, 0));
        timetable.setEndTime(LocalTime.of(9, 0));
        timetable.setRoomNumber("Room 1");
        return timetable;
    }
}
