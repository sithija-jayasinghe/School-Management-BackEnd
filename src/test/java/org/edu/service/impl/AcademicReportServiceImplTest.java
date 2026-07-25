package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.edu.dto.AcademicReportDTO;
import org.edu.dto.AcademicReportReadinessDTO;
import org.edu.dto.AttendanceSummaryDTO;
import org.edu.dto.request.AcademicReportGenerateRequest;
import org.edu.dto.request.AcademicReportUpdateRequest;
import org.edu.entity.AcademicReport;
import org.edu.entity.AcademicReportSubject;
import org.edu.entity.AcademicTerm;
import org.edu.entity.AcademicYear;
import org.edu.entity.Exam;
import org.edu.entity.Staff;
import org.edu.entity.Student;
import org.edu.entity.StudentEnrollment;
import org.edu.entity.StudentMark;
import org.edu.entity.Subject;
import org.edu.entity.User;
import org.edu.repository.AcademicReportRepository;
import org.edu.repository.AcademicTermRepository;
import org.edu.repository.ClassRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentEnrollmentRepository;
import org.edu.repository.StudentMarkRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.TimetableRepository;
import org.edu.repository.UserRepository;
import org.edu.service.AcademicReportPdfService;
import org.edu.service.AttendanceService;
import org.edu.util.AcademicReportStatus;
import org.edu.util.EnrollmentStatus;
import org.edu.util.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AcademicReportServiceImplTest {

    @Mock
    private AcademicReportRepository academicReportRepository;
    @Mock
    private AcademicTermRepository academicTermRepository;
    @Mock
    private StudentEnrollmentRepository studentEnrollmentRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StudentMarkRepository studentMarkRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private ClassRepository classRepository;
    @Mock
    private TimetableRepository timetableRepository;
    @Mock
    private AttendanceService attendanceService;
    @Mock
    private AcademicReportPdfService academicReportPdfService;

    private AcademicReportServiceImpl academicReportService;

    @BeforeEach
    void setUp() {
        academicReportService = new AcademicReportServiceImpl(
                academicReportRepository,
                academicTermRepository,
                studentEnrollmentRepository,
                studentRepository,
                studentMarkRepository,
                userRepository,
                staffRepository,
                classRepository,
                timetableRepository,
                attendanceService,
                academicReportPdfService
        );
    }

    @Test
    void shouldGenerateTermReportSnapshot() {
        User admin = user(100L, Role.ADMIN);
        Student student = student(20L, 30L);
        AcademicTerm term = term(40L);
        Subject math = subject(50L, "MATH", "Mathematics");
        Subject science = subject(51L, "SCI", "Science");
        List<StudentMark> marks = List.of(
                mark(student, exam(60L, term, student.getCurrentClass(), math, "100"), "80", true),
                mark(student, exam(61L, term, student.getCurrentClass(), math, "50"), "35", true),
                mark(student, exam(62L, term, student.getCurrentClass(), science, "100"), "45", true)
        );

        when(userRepository.findById(100L)).thenReturn(Optional.of(admin));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));
        when(academicTermRepository.findByIdAndActiveTrue(40L)).thenReturn(Optional.of(term));
        when(studentEnrollmentRepository.findByStudentIdAndAcademicYearIdAndStatusOrderByStartDateDesc(20L, 5L, EnrollmentStatus.ACTIVE))
                .thenReturn(List.of(enrollment(student, term)));
        when(academicReportRepository.existsByStudentIdAndAcademicTermId(20L, 40L)).thenReturn(false);
        when(studentMarkRepository.findReportMarksByStudentIdAndAcademicTermId(20L, 40L)).thenReturn(marks);
        when(attendanceService.getStudentAttendanceSummary(null, 20L, term.getStartDate(), term.getEndDate()))
                .thenReturn(new AttendanceSummaryDTO(
                        20L,
                        "Student User",
                        term.getStartDate(),
                        term.getEndDate(),
                        50,
                        42,
                        4,
                        2,
                        2,
                        88.0
                ));
        when(academicReportRepository.save(org.mockito.ArgumentMatchers.any(AcademicReport.class)))
                .thenAnswer(invocation -> {
                    AcademicReport report = invocation.getArgument(0);
                    report.setId(1L);
                    return report;
                });

        AcademicReportDTO report = academicReportService.generateReport(
                100L,
                new AcademicReportGenerateRequest(20L, 40L, "Good progress", "Keep improving")
        );

        assertEquals(2, report.getSubjectCount());
        assertEquals(new BigDecimal("64.00"), report.getOverallPercentage());
        assertEquals("C", report.getOverallGrade());
        assertEquals(new BigDecimal("88.00"), report.getAttendancePercentage());
        assertEquals(new BigDecimal("76.67"), report.getSubjects().get(0).getPercentage());
    }

    @Test
    void shouldPreventDuplicateStudentTermReport() {
        User admin = user(100L, Role.ADMIN);
        Student student = student(20L, 30L);
        AcademicTerm term = term(40L);

        when(userRepository.findById(100L)).thenReturn(Optional.of(admin));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));
        when(academicTermRepository.findByIdAndActiveTrue(40L)).thenReturn(Optional.of(term));
        when(studentEnrollmentRepository.findByStudentIdAndAcademicYearIdAndStatusOrderByStartDateDesc(20L, 5L, EnrollmentStatus.ACTIVE))
                .thenReturn(List.of(enrollment(student, term)));
        when(academicReportRepository.existsByStudentIdAndAcademicTermId(20L, 40L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> academicReportService.generateReport(
                100L,
                new AcademicReportGenerateRequest(20L, 40L, null, null)
        ));
    }

    @Test
    void shouldReturnReadinessChecklistForReportGeneration() {
        User admin = user(100L, Role.ADMIN);
        Student student = student(20L, 30L);
        AcademicTerm term = term(40L);
        Subject math = subject(50L, "MATH", "Mathematics");
        List<StudentMark> marks = List.of(
                mark(student, exam(60L, term, student.getCurrentClass(), math, "100"), "80", true)
        );

        when(userRepository.findById(100L)).thenReturn(Optional.of(admin));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));
        when(academicTermRepository.findByIdAndActiveTrue(40L)).thenReturn(Optional.of(term));
        when(studentMarkRepository.findReportMarksByStudentIdAndAcademicTermId(20L, 40L)).thenReturn(marks);
        when(attendanceService.getStudentAttendanceSummary(null, 20L, term.getStartDate(), term.getEndDate()))
                .thenReturn(new AttendanceSummaryDTO(20L, "Student User", term.getStartDate(), term.getEndDate(), 12, 10, 1, 1, 0, 91.67));
        when(studentEnrollmentRepository.findByStudentIdAndAcademicYearIdAndStatusOrderByStartDateDesc(20L, 5L, EnrollmentStatus.ACTIVE))
                .thenReturn(List.of(enrollment(student, term)));
        when(academicReportRepository.existsByStudentIdAndAcademicTermId(20L, 40L)).thenReturn(false);

        AcademicReportReadinessDTO readiness = academicReportService.checkReportReadiness(100L, 20L, 40L);

        assertTrue(readiness.isCanGenerate());
        assertEquals(1, readiness.getSubjectCount());
        assertEquals(1, readiness.getMarkCount());
        assertEquals(12, readiness.getAttendanceRecordCount());
        assertTrue(readiness.getItems().stream().allMatch(AcademicReportReadinessDTO.AcademicReportReadinessItemDTO::isReady));
    }

    @Test
    void shouldBlockReadinessWhenAttendanceIsMissing() {
        User admin = user(100L, Role.ADMIN);
        Student student = student(20L, 30L);
        AcademicTerm term = term(40L);
        Subject math = subject(50L, "MATH", "Mathematics");
        List<StudentMark> marks = List.of(
                mark(student, exam(60L, term, student.getCurrentClass(), math, "100"), "80", true)
        );

        when(userRepository.findById(100L)).thenReturn(Optional.of(admin));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));
        when(academicTermRepository.findByIdAndActiveTrue(40L)).thenReturn(Optional.of(term));
        when(studentMarkRepository.findReportMarksByStudentIdAndAcademicTermId(20L, 40L)).thenReturn(marks);
        when(attendanceService.getStudentAttendanceSummary(null, 20L, term.getStartDate(), term.getEndDate()))
                .thenReturn(new AttendanceSummaryDTO(20L, "Student User", term.getStartDate(), term.getEndDate(), 0, 0, 0, 0, 0, 0.0));
        when(studentEnrollmentRepository.findByStudentIdAndAcademicYearIdAndStatusOrderByStartDateDesc(20L, 5L, EnrollmentStatus.ACTIVE))
                .thenReturn(List.of(enrollment(student, term)));
        when(academicReportRepository.existsByStudentIdAndAcademicTermId(20L, 40L)).thenReturn(false);

        AcademicReportReadinessDTO readiness = academicReportService.checkReportReadiness(100L, 20L, 40L);

        assertEquals(false, readiness.isCanGenerate());
        assertEquals(0, readiness.getAttendanceRecordCount());
    }

    @Test
    void shouldAllowOnlyClassTeacherToPublishForTeacherRole() {
        User teacherUser = user(100L, Role.TEACHER);
        Staff teacher = staff(10L, teacherUser);
        AcademicReport report = report(1L, AcademicReportStatus.DRAFT, teacherUser);

        when(userRepository.findById(100L)).thenReturn(Optional.of(teacherUser));
        when(academicReportRepository.findDetailById(1L)).thenReturn(Optional.of(report));
        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(teacher));
        when(classRepository.existsByIdAndClassTeacherIdAndActiveTrue(30L, 10L)).thenReturn(false);

        assertThrows(org.edu.exception.ResourceNotFoundException.class,
                () -> academicReportService.publishReport(100L, 1L));
    }

    @Test
    void shouldPublishDraftForClassTeacher() {
        User teacherUser = user(100L, Role.TEACHER);
        Staff teacher = staff(10L, teacherUser);
        AcademicReport report = report(1L, AcademicReportStatus.DRAFT, teacherUser);

        when(userRepository.findById(100L)).thenReturn(Optional.of(teacherUser));
        when(academicReportRepository.findDetailById(1L)).thenReturn(Optional.of(report));
        when(staffRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(teacher));
        when(classRepository.existsByIdAndClassTeacherIdAndActiveTrue(30L, 10L)).thenReturn(true);
        when(academicReportRepository.save(report)).thenReturn(report);

        AcademicReportDTO published = academicReportService.publishReport(100L, 1L);

        assertEquals(AcademicReportStatus.PUBLISHED, published.getStatus());
        assertEquals(100L, published.getPublishedByUserId());
        assertTrue(published.getPublishedAt() != null);
    }

    @Test
    void shouldRejectChangesToPublishedReport() {
        User admin = user(100L, Role.ADMIN);
        AcademicReport report = report(1L, AcademicReportStatus.PUBLISHED, admin);

        when(userRepository.findById(100L)).thenReturn(Optional.of(admin));
        when(academicReportRepository.findDetailById(1L)).thenReturn(Optional.of(report));

        assertThrows(IllegalStateException.class, () -> academicReportService.updateReport(
                100L,
                1L,
                new AcademicReportUpdateRequest("Changed", null)
        ));
    }

    private User user(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setName(role.name() + " User");
        user.setEmail(role.name().toLowerCase() + "@example.com");
        user.setRole(role);
        return user;
    }

    private Staff staff(Long id, User user) {
        Staff staff = new Staff();
        staff.setId(id);
        staff.setUser(user);
        staff.setName("Teacher User");
        staff.setStaffId("T-001");
        staff.setActive(true);
        return staff;
    }

    private Student student(Long id, Long classId) {
        org.edu.entity.Class studentClass = new org.edu.entity.Class();
        studentClass.setId(classId);
        studentClass.setName("Grade 10A");
        studentClass.setActive(true);

        Student student = new Student();
        student.setId(id);
        student.setName("Student User");
        student.setCurrentClass(studentClass);
        student.setActive(true);
        return student;
    }

    private StudentEnrollment enrollment(Student student, AcademicTerm term) {
        StudentEnrollment enrollment = new StudentEnrollment();
        enrollment.setStudent(student);
        enrollment.setAcademicYear(term.getAcademicYear());
        enrollment.setStudentClass(student.getCurrentClass());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        return enrollment;
    }

    private AcademicTerm term(Long id) {
        AcademicYear year = new AcademicYear();
        year.setId(5L);
        year.setName("2026");
        year.setActive(true);

        AcademicTerm term = new AcademicTerm();
        term.setId(id);
        term.setName("Term 1");
        term.setAcademicYear(year);
        term.setStartDate(LocalDate.of(2026, 1, 1));
        term.setEndDate(LocalDate.of(2026, 4, 30));
        term.setActive(true);
        return term;
    }

    private Subject subject(Long id, String code, String name) {
        Subject subject = new Subject();
        subject.setId(id);
        subject.setCode(code);
        subject.setName(name);
        return subject;
    }

    private Exam exam(
            Long id,
            AcademicTerm term,
            org.edu.entity.Class studentClass,
            Subject subject,
            String maxMarks
    ) {
        Exam exam = new Exam();
        exam.setId(id);
        exam.setAcademicTerm(term);
        exam.setAcademicYear(term.getAcademicYear());
        exam.setStudentClass(studentClass);
        exam.setSubject(subject);
        exam.setName(subject.getName() + " Exam");
        exam.setMaxMarks(new BigDecimal(maxMarks));
        exam.setPassMarks(new BigDecimal("40"));
        exam.setActive(true);
        return exam;
    }

    private StudentMark mark(Student student, Exam exam, String marks, boolean passed) {
        StudentMark mark = new StudentMark();
        mark.setStudent(student);
        mark.setExam(exam);
        mark.setMarksObtained(new BigDecimal(marks));
        mark.setPassed(passed);
        return mark;
    }

    private AcademicReport report(Long id, AcademicReportStatus status, User generatedBy) {
        Student student = student(20L, 30L);
        AcademicReport report = new AcademicReport();
        report.setId(id);
        report.setStudent(student);
        report.setStudentClass(student.getCurrentClass());
        report.setAcademicTerm(term(40L));
        report.setGeneratedBy(generatedBy);
        report.setStatus(status);
        report.setOverallPercentage(new BigDecimal("75.00"));
        report.setOverallGrade("A");
        report.setSubjectCount(1);
        report.setPassedSubjectCount(1);
        report.setFailedSubjectCount(0);
        report.setAttendancePercentage(new BigDecimal("90.00"));

        AcademicReportSubject subject = new AcademicReportSubject();
        subject.setSubjectId(50L);
        subject.setSubjectCode("MATH");
        subject.setSubjectName("Mathematics");
        subject.setExamCount(1);
        subject.setTotalMarksObtained(new BigDecimal("75.00"));
        subject.setTotalMaxMarks(new BigDecimal("100.00"));
        subject.setPercentage(new BigDecimal("75.00"));
        subject.setGrade("A");
        subject.setPassed(true);
        report.replaceSubjects(List.of(subject));
        return report;
    }
}
