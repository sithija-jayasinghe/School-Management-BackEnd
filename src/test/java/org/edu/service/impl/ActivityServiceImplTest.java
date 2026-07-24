package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.edu.dto.activity.ActivitySaveRequest;
import org.edu.dto.activity.ActivityTeacherAssignmentSaveRequest;
import org.edu.entity.AcademicYear;
import org.edu.entity.Activity;
import org.edu.entity.ActivityTeacherAssignment;
import org.edu.entity.Staff;
import org.edu.entity.User;
import org.edu.repository.AcademicYearRepository;
import org.edu.repository.ActivityRepository;
import org.edu.repository.ActivityTeacherAssignmentRepository;
import org.edu.repository.StaffRepository;
import org.edu.util.ActivityCategory;
import org.edu.util.ActivityTeacherRole;
import org.edu.util.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityServiceImplTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityTeacherAssignmentRepository assignmentRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private AcademicYearRepository academicYearRepository;

    private ActivityServiceImpl activityService;

    @BeforeEach
    void setUp() {
        activityService = new ActivityServiceImpl(
                activityRepository,
                assignmentRepository,
                staffRepository,
                academicYearRepository
        );
    }

    @Test
    void shouldNormalizeAndCreateActivity() {
        ActivitySaveRequest request = activityRequest();
        request.setCode("  cric  ");
        request.setName("  Cricket  ");
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(assignmentRepository.findByActivityIdAndActiveTrueOrderByPrimaryResponsibleDescStaffNameAsc(1L))
                .thenReturn(List.of());

        var result = activityService.createActivity(request);

        assertEquals("CRIC", result.getCode());
        assertEquals("Cricket", result.getName());
        assertTrue(result.isActive());
    }

    @Test
    void shouldRequireFirstTeacherForYearToBePrimary() {
        Activity activity = activity(1L);
        Staff teacher = teacher(2L, Role.TEACHER);
        AcademicYear year = academicYear(3L);
        ActivityTeacherAssignmentSaveRequest request = assignmentRequest(2L, 3L, false);

        when(activityRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(activity));
        when(staffRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(teacher));
        when(academicYearRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(year));
        when(assignmentRepository.findByActivityIdAndStaffIdAndAcademicYearId(1L, 2L, 3L))
                .thenReturn(Optional.empty());
        when(assignmentRepository.countByActivityIdAndAcademicYearIdAndActiveTrue(1L, 3L)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class, () -> activityService.assignTeacher(1L, request));
    }

    @Test
    void shouldPromoteNewPrimaryAndDemotePreviousPrimary() {
        Activity activity = activity(1L);
        AcademicYear year = academicYear(3L);
        Staff newTeacher = teacher(2L, Role.TEACHER);
        Staff currentTeacher = teacher(4L, Role.TEACHER);
        ActivityTeacherAssignment currentPrimary = assignment(10L, activity, currentTeacher, year, true);
        ActivityTeacherAssignmentSaveRequest request = assignmentRequest(2L, 3L, true);

        when(activityRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(activity));
        when(staffRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(newTeacher));
        when(academicYearRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(year));
        when(assignmentRepository.findByActivityIdAndStaffIdAndAcademicYearId(1L, 2L, 3L))
                .thenReturn(Optional.empty());
        when(assignmentRepository.countByActivityIdAndAcademicYearIdAndActiveTrue(1L, 3L)).thenReturn(1L);
        when(assignmentRepository.findByActivityIdAndAcademicYearIdAndPrimaryResponsibleTrueAndActiveTrue(1L, 3L))
                .thenReturn(Optional.of(currentPrimary));
        when(assignmentRepository.save(any(ActivityTeacherAssignment.class))).thenAnswer(invocation -> {
            ActivityTeacherAssignment saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });

        var result = activityService.assignTeacher(1L, request);

        assertTrue(result.isPrimaryResponsible());
        assertFalse(currentPrimary.isPrimaryResponsible());
    }

    @Test
    void shouldRejectStaffWithoutTeacherRole() {
        Activity activity = activity(1L);
        Staff administrator = teacher(2L, Role.ADMIN);
        ActivityTeacherAssignmentSaveRequest request = assignmentRequest(2L, 3L, true);

        when(activityRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(activity));
        when(staffRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(administrator));

        assertThrows(IllegalArgumentException.class, () -> activityService.assignTeacher(1L, request));
    }

    @Test
    void shouldRejectDeactivatingPrimaryTeacherAssignment() {
        Activity activity = activity(1L);
        ActivityTeacherAssignment primary = assignment(
                10L,
                activity,
                teacher(2L, Role.TEACHER),
                academicYear(3L),
                true
        );
        when(activityRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(activity));
        when(assignmentRepository.findByIdAndActivityIdAndActiveTrue(10L, 1L)).thenReturn(Optional.of(primary));

        assertThrows(IllegalStateException.class, () -> activityService.deactivateTeacherAssignment(1L, 10L));
        assertTrue(primary.isActive());
    }

    @Test
    void shouldDeactivateActivityAndAllTeacherAssignments() {
        Activity activity = activity(1L);
        ActivityTeacherAssignment primary = assignment(
                10L,
                activity,
                teacher(2L, Role.TEACHER),
                academicYear(3L),
                true
        );
        when(activityRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(activity));
        when(assignmentRepository.findByActivityIdAndActiveTrueOrderByPrimaryResponsibleDescStaffNameAsc(1L))
                .thenReturn(List.of(primary));

        activityService.deactivateActivity(1L);

        assertFalse(activity.isActive());
        assertFalse(primary.isActive());
        verify(assignmentRepository).findByActivityIdAndActiveTrueOrderByPrimaryResponsibleDescStaffNameAsc(1L);
    }

    private ActivitySaveRequest activityRequest() {
        ActivitySaveRequest request = new ActivitySaveRequest();
        request.setCode("CRIC");
        request.setName("Cricket");
        request.setCategory(ActivityCategory.SPORT);
        request.setDescription("School cricket programme");
        request.setVenue("School grounds");
        return request;
    }

    private ActivityTeacherAssignmentSaveRequest assignmentRequest(Long staffId, Long yearId, boolean primary) {
        ActivityTeacherAssignmentSaveRequest request = new ActivityTeacherAssignmentSaveRequest();
        request.setStaffId(staffId);
        request.setAcademicYearId(yearId);
        request.setResponsibilityRole(ActivityTeacherRole.TEACHER_IN_CHARGE);
        request.setPrimaryResponsible(primary);
        return request;
    }

    private Activity activity(Long id) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setCode("CRIC");
        activity.setName("Cricket");
        activity.setCategory(ActivityCategory.SPORT);
        activity.setActive(true);
        return activity;
    }

    private Staff teacher(Long id, Role role) {
        User user = new User();
        user.setId(id + 100L);
        user.setName("Teacher " + id);
        user.setEmail("teacher" + id + "@school.lk");
        user.setRole(role);

        Staff staff = new Staff();
        staff.setId(id);
        staff.setUser(user);
        staff.setStaffId("T" + id);
        staff.setName("Teacher " + id);
        staff.setDesignation("Teacher");
        staff.setActive(true);
        return staff;
    }

    private AcademicYear academicYear(Long id) {
        AcademicYear year = new AcademicYear();
        year.setId(id);
        year.setName("2026");
        year.setStartDate(LocalDate.of(2026, 1, 1));
        year.setEndDate(LocalDate.of(2026, 12, 31));
        year.setCurrent(true);
        year.setActive(true);
        return year;
    }

    private ActivityTeacherAssignment assignment(
            Long id,
            Activity activity,
            Staff staff,
            AcademicYear year,
            boolean primary
    ) {
        ActivityTeacherAssignment assignment = new ActivityTeacherAssignment();
        assignment.setId(id);
        assignment.setActivity(activity);
        assignment.setStaff(staff);
        assignment.setAcademicYear(year);
        assignment.setResponsibilityRole(ActivityTeacherRole.TEACHER_IN_CHARGE);
        assignment.setPrimaryResponsible(primary);
        assignment.setActive(true);
        return assignment;
    }
}
