package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.edu.dto.parentportal.ParentPortalDashboardDTO;
import org.edu.dto.parentportal.ParentPortalStudentDetailDTO;
import org.edu.dto.parentportal.ParentPortalAttendanceDTO;
import org.edu.dto.parentportal.ParentPortalTimetableEntryDTO;
import org.edu.entity.Parent;
import org.edu.entity.ParentStudent;
import org.edu.entity.Staff;
import org.edu.entity.Student;
import org.edu.entity.Subject;
import org.edu.entity.Timetable;
import org.edu.entity.User;
import org.edu.exception.ResourceNotFoundException;
import org.edu.repository.ParentRepository;
import org.edu.repository.ParentStudentRepository;
import org.edu.repository.TimetableRepository;
import org.edu.service.AttendanceService;
import org.edu.util.AttendanceStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParentPortalServiceImplTest {

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private ParentStudentRepository parentStudentRepository;

    @Mock
    private TimetableRepository timetableRepository;

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private ParentPortalServiceImpl parentPortalService;

    @Test
    void shouldBuildDashboardForAuthenticatedParentOnly() {
        Parent parent = parentWithUser(10L, 100L);
        ParentStudent link = parentStudentLink(parent, studentWithClass(20L, 30L));

        when(parentRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(parent));
        when(parentStudentRepository.findActiveStudentLinksByParentId(10L)).thenReturn(List.of(link));

        ParentPortalDashboardDTO dashboard = parentPortalService.getDashboard(100L);

        assertEquals(10L, dashboard.getProfile().getParentId());
        assertEquals(1, dashboard.getLinkedStudentCount());
        assertEquals(20L, dashboard.getStudents().get(0).getStudentId());
        verify(parentStudentRepository).findActiveStudentLinksByParentId(10L);
    }

    @Test
    void shouldRejectAccessToStudentNotLinkedToAuthenticatedParent() {
        Parent parent = parentWithUser(10L, 100L);

        when(parentRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(parent));
        when(parentStudentRepository.findActiveStudentLinkByParentIdAndStudentId(10L, 999L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> parentPortalService.getStudentDetail(100L, 999L));
    }

    @Test
    void shouldReturnStudentDetailWithSubjects() {
        Parent parent = parentWithUser(10L, 100L);
        Student student = studentWithClass(20L, 30L);
        Subject subject = subject(40L, "MATH", "Mathematics");
        student.getCurrentClass().setSubjects(List.of(subject));

        ParentStudent link = parentStudentLink(parent, student);

        when(parentRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(parent));
        when(parentStudentRepository.findActiveStudentLinkByParentIdAndStudentId(10L, 20L))
                .thenReturn(Optional.of(link));

        ParentPortalStudentDetailDTO detail = parentPortalService.getStudentDetail(100L, 20L);

        assertEquals(20L, detail.getStudentId());
        assertEquals("Grade 10A", detail.getClassName());
        assertEquals(1, detail.getSubjects().size());
        assertEquals("Mathematics", detail.getSubjects().get(0).getName());
    }

    @Test
    void shouldReturnTimetableForLinkedStudentClass() {
        Parent parent = parentWithUser(10L, 100L);
        Student student = studentWithClass(20L, 30L);
        ParentStudent link = parentStudentLink(parent, student);
        Timetable timetable = timetable(student.getCurrentClass(), subject(40L, "SCI", "Science"));

        when(parentRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(parent));
        when(parentStudentRepository.findActiveStudentLinkByParentIdAndStudentId(10L, 20L))
                .thenReturn(Optional.of(link));
        when(timetableRepository.findPortalTimetableByClassId(30L)).thenReturn(List.of(timetable));

        List<ParentPortalTimetableEntryDTO> timetableEntries = parentPortalService.getStudentTimetable(100L, 20L);

        assertEquals(1, timetableEntries.size());
        assertEquals("Science", timetableEntries.get(0).getSubjectName());
        assertEquals("Nimal Perera", timetableEntries.get(0).getTeacherName());
    }

    @Test
    void shouldReturnAttendanceOnlyAfterLinkedStudentAuthorization() {
        Parent parent = parentWithUser(10L, 100L);
        Student student = studentWithClass(20L, 30L);
        ParentStudent link = parentStudentLink(parent, student);
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);

        ParentPortalAttendanceDTO attendance = new ParentPortalAttendanceDTO(
                1L,
                LocalDate.of(2026, 1, 5),
                AttendanceStatus.PRESENT,
                "Grade 10A",
                "Science",
                "Nimal Perera",
                "On time"
        );

        when(parentRepository.findByUser_IdAndActiveTrue(100L)).thenReturn(Optional.of(parent));
        when(parentStudentRepository.findActiveStudentLinkByParentIdAndStudentId(10L, 20L))
                .thenReturn(Optional.of(link));
        when(attendanceService.getPortalAttendance(20L, from, to)).thenReturn(List.of(attendance));

        List<ParentPortalAttendanceDTO> result = parentPortalService.getStudentAttendance(100L, 20L, from, to);

        assertEquals(1, result.size());
        assertEquals(AttendanceStatus.PRESENT, result.get(0).getStatus());
    }

    private Parent parentWithUser(Long parentId, Long userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail("parent@example.com");

        Parent parent = new Parent();
        parent.setId(parentId);
        parent.setUser(user);
        parent.setName("Parent User");
        parent.setPhoneNumber("0771234567");
        parent.setAddress("Colombo");
        parent.setOccupation("Engineer");
        parent.setActive(true);
        return parent;
    }

    private Student studentWithClass(Long studentId, Long classId) {
        org.edu.entity.Class studentClass = new org.edu.entity.Class();
        studentClass.setId(classId);
        studentClass.setName("Grade 10A");

        Student student = new Student();
        student.setId(studentId);
        student.setName("Student User");
        student.setDateOfBirth(LocalDate.of(2010, 1, 1));
        student.setPhoneNumber("0777654321");
        student.setCurrentClass(studentClass);
        student.setActive(true);
        return student;
    }

    private ParentStudent parentStudentLink(Parent parent, Student student) {
        ParentStudent link = new ParentStudent();
        link.setParent(parent);
        link.setStudent(student);
        link.setRelationshipType("Father");
        link.setPrimaryContact(true);
        link.setEmergencyContact(true);
        return link;
    }

    private Subject subject(Long subjectId, String code, String name) {
        Subject subject = new Subject();
        subject.setId(subjectId);
        subject.setCode(code);
        subject.setName(name);
        subject.setDescription(name + " subject");
        return subject;
    }

    private Timetable timetable(org.edu.entity.Class studentClass, Subject subject) {
        Staff staff = new Staff();
        staff.setId(50L);
        staff.setName("Nimal Perera");

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
