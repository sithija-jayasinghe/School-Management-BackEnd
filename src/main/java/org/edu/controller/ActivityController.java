package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.activity.ActivityDTO;
import org.edu.dto.activity.ActivitySaveRequest;
import org.edu.dto.activity.ActivityTeacherAssignmentDTO;
import org.edu.dto.activity.ActivityTeacherAssignmentSaveRequest;
import org.edu.service.ActivityService;
import org.edu.service.AuditLogService;
import org.edu.util.ActivityCategory;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Sports and Activities", description = "Manage sports, extracurricular activities, and responsible teachers")
@SecurityRequirement(name = "bearerAuth")
public class ActivityController {

    private final ActivityService activityService;
    private final AuditLogService auditLogService;

    @PostMapping
    @Operation(summary = "Create a sport or extracurricular activity")
    public ActivityDTO createActivity(@Valid @RequestBody ActivitySaveRequest request) {
        ActivityDTO response = activityService.createActivity(request);
        auditLogService.log(
                AuditAction.CREATE,
                AuditEntityType.ACTIVITY,
                response.getId(),
                response.getName(),
                "Created " + response.getCategory() + " activity"
        );
        return response;
    }

    @PatchMapping("/{activityId}")
    @Operation(summary = "Update an active activity")
    public ActivityDTO updateActivity(
            @PathVariable Long activityId,
            @Valid @RequestBody ActivitySaveRequest request
    ) {
        ActivityDTO response = activityService.updateActivity(activityId, request);
        auditLogService.log(AuditAction.UPDATE, AuditEntityType.ACTIVITY, response.getId(), response.getName(), "Updated activity");
        return response;
    }

    @DeleteMapping("/{activityId}")
    @Operation(summary = "Deactivate an activity and its active teacher assignments")
    public void deactivateActivity(@PathVariable Long activityId) {
        activityService.deactivateActivity(activityId);
        auditLogService.log(
                AuditAction.DEACTIVATE,
                AuditEntityType.ACTIVITY,
                activityId,
                "Activity #" + activityId,
                "Deactivated activity"
        );
    }

    @GetMapping
    @Operation(summary = "List and filter active activities")
    public Page<ActivityDTO> getActivities(
            @RequestParam(required = false) ActivityCategory category,
            @RequestParam(required = false, defaultValue = "") String keyword,
            Pageable pageable
    ) {
        return activityService.getActivities(category, keyword, pageable);
    }

    @GetMapping("/{activityId}")
    @Operation(summary = "Get one active activity")
    public ActivityDTO getActivity(@PathVariable Long activityId) {
        return activityService.getActivity(activityId);
    }

    @GetMapping("/{activityId}/teacher-assignments")
    @Operation(summary = "List responsible teachers for an activity")
    public List<ActivityTeacherAssignmentDTO> getTeacherAssignments(@PathVariable Long activityId) {
        return activityService.getTeacherAssignments(activityId);
    }

    @PostMapping("/{activityId}/teacher-assignments")
    @Operation(summary = "Assign a responsible teacher for an academic year")
    public ActivityTeacherAssignmentDTO assignTeacher(
            @PathVariable Long activityId,
            @Valid @RequestBody ActivityTeacherAssignmentSaveRequest request
    ) {
        ActivityTeacherAssignmentDTO response = activityService.assignTeacher(activityId, request);
        auditLogService.log(
                AuditAction.LINK,
                AuditEntityType.ACTIVITY_TEACHER_ASSIGNMENT,
                response.getId(),
                response.getStaffName(),
                "Assigned teacher to " + response.getActivityName() + " for " + response.getAcademicYearName()
        );
        return response;
    }

    @PatchMapping("/{activityId}/teacher-assignments/{assignmentId}")
    @Operation(summary = "Update a teacher responsibility or promote the primary teacher")
    public ActivityTeacherAssignmentDTO updateTeacherAssignment(
            @PathVariable Long activityId,
            @PathVariable Long assignmentId,
            @Valid @RequestBody ActivityTeacherAssignmentSaveRequest request
    ) {
        ActivityTeacherAssignmentDTO response = activityService.updateTeacherAssignment(activityId, assignmentId, request);
        auditLogService.log(
                AuditAction.UPDATE,
                AuditEntityType.ACTIVITY_TEACHER_ASSIGNMENT,
                response.getId(),
                response.getStaffName(),
                "Updated activity teacher responsibility"
        );
        return response;
    }

    @DeleteMapping("/{activityId}/teacher-assignments/{assignmentId}")
    @Operation(summary = "Deactivate a non-primary teacher assignment")
    public void deactivateTeacherAssignment(
            @PathVariable Long activityId,
            @PathVariable Long assignmentId
    ) {
        activityService.deactivateTeacherAssignment(activityId, assignmentId);
        auditLogService.log(
                AuditAction.UNLINK,
                AuditEntityType.ACTIVITY_TEACHER_ASSIGNMENT,
                assignmentId,
                "Activity assignment #" + assignmentId,
                "Deactivated activity teacher assignment"
        );
    }
}
