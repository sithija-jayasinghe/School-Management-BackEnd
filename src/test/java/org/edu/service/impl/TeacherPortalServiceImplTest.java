package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.edu.dto.teacherportal.TeacherPortalDashboardDTO;
import org.edu.dto.teacherportal.TeacherPortalProfileDTO;
import org.edu.dto.teacherportal.TeacherPortalTimetableEntryDTO;
import org.edu.entity.Staff;
import org.edu.entity.Subject;
import org.edu.entity.Timetable;
import org.edu.entity.User;
import org.edu.exception.ResourceNotFoundException;
import org.edu.repository.ClassRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.TimetableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeacherPortalServiceImplTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private TimetableRepository timetableRepository;

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
        return subject;
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
