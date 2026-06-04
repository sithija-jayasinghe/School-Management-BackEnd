package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.edu.dto.AttendanceDTO;
import org.edu.dto.AttendanceSummaryDTO;
import org.edu.entity.Attendance;
import org.edu.entity.Student;
import org.edu.entity.Subject;
import org.edu.entity.Timetable;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.AttendanceMapper;
import org.edu.repository.AttendanceRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.SubjectRepository;
import org.edu.repository.TimetableRepository;
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
    private StudentRepository studentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TimetableRepository timetableRepository;

    @Mock
    private StaffRepository staffRepository;

    private AttendanceServiceImpl attendanceService;

    @BeforeEach
    void setUp() {
        AttendanceMapper attendanceMapper = Mappers.getMapper(AttendanceMapper.class);
        attendanceService = new AttendanceServiceImpl(
                attendanceRepository,
                studentRepository,
                subjectRepository,
                timetableRepository,
                staffRepository,
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

        assertThrows(IllegalStateException.class, () -> attendanceService.createAttendance(dto));
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

        assertThrows(IllegalArgumentException.class, () -> attendanceService.createAttendance(dto));
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

        AttendanceDTO saved = attendanceService.createAttendance(dto);

        assertEquals(5L, saved.getId());
        assertEquals(10L, saved.getClassId());
        assertEquals(AttendanceStatus.PRESENT, saved.getStatus());
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

        AttendanceSummaryDTO summary = attendanceService.getStudentAttendanceSummary(1L, from, to);

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
