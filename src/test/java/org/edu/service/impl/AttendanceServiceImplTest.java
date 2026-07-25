package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.edu.dto.AttendanceDTO;
import org.edu.dto.AttendanceSummaryDTO;
import org.edu.dto.request.BulkAttendanceRequest;
import org.edu.dto.request.BulkAttendanceStudentRequest;
import org.edu.entity.Attendance;
import org.edu.entity.Student;
import org.edu.entity.Subject;
import org.edu.entity.Timetable;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.AttendanceMapper;
import org.edu.repository.AttendanceRepository;
import org.edu.repository.ClassRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.SubjectRepository;
import org.edu.repository.TimetableRepository;
import org.edu.repository.UserRepository;
import org.edu.util.AttendanceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TimetableRepository timetableRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private UserRepository userRepository;

    private AttendanceServiceImpl attendanceService;

    @BeforeEach
    void setUp() {
        AttendanceMapper attendanceMapper = Mappers.getMapper(AttendanceMapper.class);
        attendanceService = new AttendanceServiceImpl(
                attendanceRepository,
                classRepository,
                studentRepository,
                subjectRepository,
                timetableRepository,
                staffRepository,
                userRepository,
                attendanceMapper
        );
    }

    @Test
    void shouldRejectDuplicateDailyAttendance() {
        AttendanceDTO dto = attendanceRequest();
        Student student = activeStudentWithClass(1L, 10L);

        when(studentRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(student));
        when(attendanceRepository.existsByStudentIdAndAttendanceDateAndTimetableIsNull(1L, dto.getAttendanceDate()))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () -> attendanceService.createAttendance(null, dto));
        verify(attendanceRepository, never()).save(org.mockito.Mockito.any(Attendance.class));
    }

    @Test
    void shouldRejectTimetableFromDifferentClass() {
        AttendanceDTO dto = attendanceRequest();
        dto.setTimetableId(99L);

        Student student = activeStudentWithClass(1L, 10L);
        Timetable timetable = new Timetable();
        timetable.setId(99L);
        timetable.setStudentClass(studentClass(20L));
        timetable.setSubject(subject(3L));

        when(studentRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(student));
        when(attendanceRepository.existsByStudentIdAndAttendanceDateAndTimetableId(1L, dto.getAttendanceDate(), 99L))
                .thenReturn(false);
        when(timetableRepository.findById(99L)).thenReturn(Optional.of(timetable));

        assertThrows(IllegalArgumentException.class, () -> attendanceService.createAttendance(null, dto));
    }

    @Test
    void shouldCreateAttendanceForActiveStudent() {
        AttendanceDTO dto = attendanceRequest();
        Student student = activeStudentWithClass(1L, 10L);

        when(studentRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(student));
        when(attendanceRepository.existsByStudentIdAndAttendanceDateAndTimetableIsNull(1L, dto.getAttendanceDate()))
                .thenReturn(false);
        when(attendanceRepository.save(org.mockito.Mockito.any(Attendance.class)))
                .thenAnswer(invocation -> {
                    Attendance attendance = invocation.getArgument(0);
                    attendance.setId(5L);
                    return attendance;
                });

        AttendanceDTO saved = attendanceService.createAttendance(null, dto);

        assertEquals(5L, saved.getId());
        assertEquals(10L, saved.getClassId());
        assertEquals(AttendanceStatus.PRESENT, saved.getStatus());
    }

    @Test
    void shouldBulkMarkClassAttendance() {
        org.edu.entity.Class studentClass = studentClass(10L);
        Student firstStudent = activeStudentWithClass(1L, 10L);
        Student secondStudent = activeStudentWithClass(2L, 10L);
        BulkAttendanceRequest request = bulkAttendanceRequest(10L, List.of(
                new BulkAttendanceStudentRequest(1L, AttendanceStatus.PRESENT, "On time"),
                new BulkAttendanceStudentRequest(2L, AttendanceStatus.ABSENT, "Sick")
        ));

        when(classRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(studentClass));
        when(studentRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(firstStudent));
        when(studentRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(secondStudent));
        when(attendanceRepository.existsByStudentIdAndAttendanceDateAndTimetableIsNull(1L, request.getAttendanceDate()))
                .thenReturn(false);
        when(attendanceRepository.existsByStudentIdAndAttendanceDateAndTimetableIsNull(2L, request.getAttendanceDate()))
                .thenReturn(false);
        when(attendanceRepository.saveAll(org.mockito.Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<Attendance> records = new ArrayList<>(invocation.getArgument(0));
                    records.get(0).setId(101L);
                    records.get(1).setId(102L);
                    return records;
                });

        List<AttendanceDTO> saved = attendanceService.markClassAttendance(null, request);

        assertEquals(2, saved.size());
        assertEquals(101L, saved.get(0).getId());
        assertEquals(AttendanceStatus.ABSENT, saved.get(1).getStatus());
    }

    @Test
    void shouldRejectBulkAttendanceWhenStudentIsOutsideClass() {
        org.edu.entity.Class studentClass = studentClass(10L);
        Student student = activeStudentWithClass(1L, 20L);
        BulkAttendanceRequest request = bulkAttendanceRequest(10L, List.of(
                new BulkAttendanceStudentRequest(1L, AttendanceStatus.PRESENT, null)
        ));

        when(classRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(studentClass));
        when(studentRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(student));

        assertThrows(IllegalArgumentException.class, () -> attendanceService.markClassAttendance(null, request));
        verify(attendanceRepository, never()).saveAll(org.mockito.Mockito.anyList());
    }

    @Test
    void shouldRejectDuplicateStudentInsideBulkAttendanceRequest() {
        org.edu.entity.Class studentClass = studentClass(10L);
        BulkAttendanceRequest request = bulkAttendanceRequest(10L, List.of(
                new BulkAttendanceStudentRequest(1L, AttendanceStatus.PRESENT, null),
                new BulkAttendanceStudentRequest(1L, AttendanceStatus.ABSENT, null)
        ));

        when(classRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(studentClass));

        assertThrows(IllegalArgumentException.class, () -> attendanceService.markClassAttendance(null, request));
        verify(attendanceRepository, never()).saveAll(org.mockito.Mockito.anyList());
    }

    @Test
    void shouldBuildStudentAttendanceSummary() {
        Student student = activeStudentWithClass(1L, 10L);
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);

        when(studentRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(student));
        when(attendanceRepository.findByStudentIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(1L, from, to))
                .thenReturn(List.of(
                        attendance(student, AttendanceStatus.PRESENT),
                        attendance(student, AttendanceStatus.LATE),
                        attendance(student, AttendanceStatus.ABSENT),
                        attendance(student, AttendanceStatus.EXCUSED)
                ));

        AttendanceSummaryDTO summary = attendanceService.getStudentAttendanceSummary(null, 1L, from, to);

        assertEquals(4, summary.getTotalRecords());
        assertEquals(1, summary.getPresentCount());
        assertEquals(1, summary.getLateCount());
        assertEquals(1, summary.getAbsentCount());
        assertEquals(1, summary.getExcusedCount());
        assertEquals(75.0, summary.getAttendancePercentage());
    }

    @Test
    void shouldRejectSummaryForMissingStudent() {
        when(studentRepository.findByIdAndActiveTrue(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.getStudentAttendanceSummary(
                        null,
                        404L,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31)
                ));
    }

    private AttendanceDTO attendanceRequest() {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setStudentId(1L);
        dto.setAttendanceDate(LocalDate.of(2026, 1, 5));
        dto.setStatus(AttendanceStatus.PRESENT);
        dto.setRemarks("On time");
        return dto;
    }

    private BulkAttendanceRequest bulkAttendanceRequest(
            Long classId,
            List<BulkAttendanceStudentRequest> students
    ) {
        BulkAttendanceRequest request = new BulkAttendanceRequest();
        request.setClassId(classId);
        request.setAttendanceDate(LocalDate.of(2026, 1, 5));
        request.setStudents(students);
        return request;
    }

    private Student activeStudentWithClass(Long studentId, Long classId) {
        Student student = new Student();
        student.setId(studentId);
        student.setName("Student User");
        student.setActive(true);
        student.setCurrentClass(studentClass(classId));
        return student;
    }

    private org.edu.entity.Class studentClass(Long classId) {
        org.edu.entity.Class studentClass = new org.edu.entity.Class();
        studentClass.setId(classId);
        studentClass.setName("Grade 10A");
        return studentClass;
    }

    private Subject subject(Long subjectId) {
        Subject subject = new Subject();
        subject.setId(subjectId);
        subject.setCode("MATH");
        subject.setName("Mathematics");
        return subject;
    }

    private Attendance attendance(Student student, AttendanceStatus status) {
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setStudentClass(student.getCurrentClass());
        attendance.setAttendanceDate(LocalDate.of(2026, 1, 5));
        attendance.setStatus(status);
        return attendance;
    }
}
