package org.edu.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.activity.ActivityDTO;
import org.edu.dto.activity.ActivitySaveRequest;
import org.edu.dto.activity.ActivityTeacherAssignmentDTO;
import org.edu.dto.activity.ActivityTeacherAssignmentSaveRequest;
import org.edu.entity.AcademicYear;
import org.edu.entity.Activity;
import org.edu.entity.ActivityTeacherAssignment;
import org.edu.entity.Staff;
import org.edu.exception.ResourceNotFoundException;
import org.edu.repository.AcademicYearRepository;
import org.edu.repository.ActivityRepository;
import org.edu.repository.ActivityTeacherAssignmentRepository;
import org.edu.repository.StaffRepository;
import org.edu.service.ActivityService;
import org.edu.util.ActivityCategory;
import org.edu.util.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityTeacherAssignmentRepository assignmentRepository;
    private final StaffRepository staffRepository;
    private final AcademicYearRepository academicYearRepository;

    @Override
    public ActivityDTO createActivity(ActivitySaveRequest input) {
        validateUniqueActivity(input, null);
        Activity activity = new Activity();
        applyActivity(activity, input);
        activity.setActive(true);
        return toActivityDto(activityRepository.save(activity));
    }

    @Override
    public ActivityDTO updateActivity(Long activityId, ActivitySaveRequest input) {
        Activity activity = getActiveActivity(activityId);
        validateUniqueActivity(input, activityId);
        applyActivity(activity, input);
        return toActivityDto(activity);
    }

    @Override
    public void deactivateActivity(Long activityId) {
        Activity activity = getActiveActivity(activityId);
        activity.setActive(false);
        assignmentRepository.findByActivityIdAndActiveTrueOrderByPrimaryResponsibleDescStaffNameAsc(activityId)
                .forEach(assignment -> assignment.setActive(false));
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityDTO getActivity(Long activityId) {
        return toActivityDto(getActiveActivity(activityId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityDTO> getActivities(ActivityCategory category, String keyword, Pageable pageable) {
        return activityRepository.searchActive(category, normalizeKeyword(keyword), pageable)
                .map(this::toActivityDto);
    }

    @Override
    public ActivityTeacherAssignmentDTO assignTeacher(
            Long activityId,
            ActivityTeacherAssignmentSaveRequest input
    ) {
        Activity activity = getActiveActivity(activityId);
        Staff teacher = getActiveTeacher(input.getStaffId());
        AcademicYear academicYear = getActiveAcademicYear(input.getAcademicYearId());

        ActivityTeacherAssignment assignment = assignmentRepository
                .findByActivityIdAndStaffIdAndAcademicYearId(activityId, teacher.getId(), academicYear.getId())
                .orElseGet(ActivityTeacherAssignment::new);
        if (assignment.getId() != null && assignment.isActive()) {
            throw new IllegalStateException("Teacher is already assigned to this activity for the academic year");
        }

        long yearAssignmentCount = assignmentRepository.countByActivityIdAndAcademicYearIdAndActiveTrue(
                activityId,
                academicYear.getId()
        );
        if (yearAssignmentCount == 0 && !input.isPrimaryResponsible()) {
            throw new IllegalArgumentException("The first teacher assigned for an academic year must be primary");
        }

        assignment.setActivity(activity);
        assignment.setStaff(teacher);
        assignment.setAcademicYear(academicYear);
        assignment.setResponsibilityRole(input.getResponsibilityRole());
        assignment.setActive(true);
        applyPrimaryResponsibility(assignment, input.isPrimaryResponsible());
        return toAssignmentDto(assignmentRepository.save(assignment));
    }

    @Override
    public ActivityTeacherAssignmentDTO updateTeacherAssignment(
            Long activityId,
            Long assignmentId,
            ActivityTeacherAssignmentSaveRequest input
    ) {
        getActiveActivity(activityId);
        ActivityTeacherAssignment assignment = getActiveAssignment(activityId, assignmentId);
        if (!assignment.getStaff().getId().equals(input.getStaffId())
                || !assignment.getAcademicYear().getId().equals(input.getAcademicYearId())) {
            throw new IllegalArgumentException("Teacher and academic year cannot be changed on an existing assignment");
        }
        if (assignment.isPrimaryResponsible() && !input.isPrimaryResponsible()) {
            throw new IllegalStateException("Promote another teacher before removing primary responsibility");
        }

        assignment.setResponsibilityRole(input.getResponsibilityRole());
        applyPrimaryResponsibility(assignment, input.isPrimaryResponsible());
        return toAssignmentDto(assignment);
    }

    @Override
    public void deactivateTeacherAssignment(Long activityId, Long assignmentId) {
        getActiveActivity(activityId);
        ActivityTeacherAssignment assignment = getActiveAssignment(activityId, assignmentId);
        if (assignment.isPrimaryResponsible()) {
            throw new IllegalStateException("Promote another teacher before deactivating the primary assignment");
        }
        assignment.setActive(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityTeacherAssignmentDTO> getTeacherAssignments(Long activityId) {
        getActiveActivity(activityId);
        return assignmentRepository.findByActivityIdAndActiveTrueOrderByPrimaryResponsibleDescStaffNameAsc(activityId)
                .stream()
                .map(this::toAssignmentDto)
                .toList();
    }

    private void applyPrimaryResponsibility(ActivityTeacherAssignment assignment, boolean primary) {
        if (!primary) {
            assignment.setPrimaryResponsible(false);
            return;
        }

        assignmentRepository.findByActivityIdAndAcademicYearIdAndPrimaryResponsibleTrueAndActiveTrue(
                        assignment.getActivity().getId(),
                        assignment.getAcademicYear().getId()
                )
                .filter(existing -> !existing.getId().equals(assignment.getId()))
                .ifPresent(existing -> existing.setPrimaryResponsible(false));
        assignment.setPrimaryResponsible(true);
    }

    private void validateUniqueActivity(ActivitySaveRequest input, Long currentId) {
        String code = input.getCode().trim();
        String name = input.getName().trim();
        boolean duplicateCode = currentId == null
                ? activityRepository.existsByCodeIgnoreCase(code)
                : activityRepository.existsByCodeIgnoreCaseAndIdNot(code, currentId);
        boolean duplicateName = currentId == null
                ? activityRepository.existsByNameIgnoreCase(name)
                : activityRepository.existsByNameIgnoreCaseAndIdNot(name, currentId);
        if (duplicateCode) {
            throw new IllegalStateException("An activity with this code already exists");
        }
        if (duplicateName) {
            throw new IllegalStateException("An activity with this name already exists");
        }
    }

    private void applyActivity(Activity activity, ActivitySaveRequest input) {
        activity.setCode(input.getCode().trim().toUpperCase());
        activity.setName(input.getName().trim());
        activity.setCategory(input.getCategory());
        activity.setDescription(normalizeNullable(input.getDescription()));
        activity.setVenue(normalizeNullable(input.getVenue()));
    }

    private Activity getActiveActivity(Long activityId) {
        return activityRepository.findByIdAndActiveTrue(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Active activity not found with id: " + activityId));
    }

    private ActivityTeacherAssignment getActiveAssignment(Long activityId, Long assignmentId) {
        return assignmentRepository.findByIdAndActivityIdAndActiveTrue(assignmentId, activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Active activity teacher assignment not found"));
    }

    private Staff getActiveTeacher(Long staffId) {
        Staff staff = staffRepository.findByIdAndActiveTrue(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Active teacher not found with id: " + staffId));
        boolean legacyTeacher = staff.getUser() != null && staff.getUser().getRole() == Role.TEACHER;
        if (!Boolean.TRUE.equals(staff.getTeachingCapable()) && !legacyTeacher) {
            throw new IllegalArgumentException("Selected staff member must be marked as teaching capable");
        }
        return staff;
    }

    private AcademicYear getActiveAcademicYear(Long academicYearId) {
        return academicYearRepository.findByIdAndActiveTrue(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("Active academic year not found with id: " + academicYearId));
    }

    private ActivityDTO toActivityDto(Activity activity) {
        List<ActivityTeacherAssignment> assignments = assignmentRepository
                .findByActivityIdAndActiveTrueOrderByPrimaryResponsibleDescStaffNameAsc(activity.getId());
        String primaryTeacherName = assignments.stream()
                .filter(ActivityTeacherAssignment::isPrimaryResponsible)
                .filter(assignment -> assignment.getAcademicYear().isCurrent())
                .map(assignment -> assignment.getStaff().getName())
                .findFirst()
                .orElseGet(() -> assignments.stream()
                        .filter(ActivityTeacherAssignment::isPrimaryResponsible)
                        .map(assignment -> assignment.getStaff().getName())
                        .findFirst()
                        .orElse(null));
        long distinctTeachers = assignments.stream()
                .map(assignment -> assignment.getStaff().getId())
                .distinct()
                .count();
        return new ActivityDTO(
                activity.getId(),
                activity.getCode(),
                activity.getName(),
                activity.getCategory(),
                activity.getDescription(),
                activity.getVenue(),
                activity.isActive(),
                distinctTeachers,
                primaryTeacherName,
                activity.getCreatedAt(),
                activity.getUpdatedAt()
        );
    }

    private ActivityTeacherAssignmentDTO toAssignmentDto(ActivityTeacherAssignment assignment) {
        return new ActivityTeacherAssignmentDTO(
                assignment.getId(),
                assignment.getActivity().getId(),
                assignment.getActivity().getCode(),
                assignment.getActivity().getName(),
                assignment.getStaff().getId(),
                assignment.getStaff().getStaffId(),
                assignment.getStaff().getName(),
                assignment.getStaff().getDesignation(),
                assignment.getAcademicYear().getId(),
                assignment.getAcademicYear().getName(),
                assignment.getResponsibilityRole(),
                assignment.isPrimaryResponsible(),
                assignment.isActive(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt()
        );
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
