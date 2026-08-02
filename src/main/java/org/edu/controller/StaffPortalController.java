package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.NoticeDTO;
import org.edu.dto.staffportal.StaffPortalProfileDTO;
import org.edu.dto.teacherleave.TeacherLeaveRequestDTO;
import org.edu.dto.teacherleave.TeacherLeaveSaveRequest;
import org.edu.security.UserPrincipal;
import org.edu.service.StaffPortalService;
import org.edu.service.TeacherLeaveService;
import org.edu.service.SchoolDayPolicyService;
import org.edu.dto.teacherportal.TeacherPortalSchoolDayHoursDTO;
import org.edu.util.TeacherLeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// DEMO-FEATURE: teacher-my-leave-type-filter START
// import org.edu.util.TeacherLeaveType;
// DEMO-FEATURE: teacher-my-leave-type-filter END
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff-portal")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
@Tag(name = "Staff Portal", description = "Common self-service APIs for non-teaching school employees")
@SecurityRequirement(name = "bearerAuth")
public class StaffPortalController {

    private final StaffPortalService staffPortalService;
    private final TeacherLeaveService leaveService;
    private final SchoolDayPolicyService schoolDayPolicyService;

    @GetMapping("/profile")
    @Operation(summary = "Get the authenticated staff profile")
    public StaffPortalProfileDTO getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return staffPortalService.getProfile(principal.getUser().getId());
    }

    @GetMapping("/notices")
    @Operation(summary = "List currently visible notices for staff")
    public List<NoticeDTO> getNotices(@AuthenticationPrincipal UserPrincipal principal) {
        return staffPortalService.getNotices(principal.getUser().getId());
    }

    @GetMapping("/school-day-hours")
    @Operation(summary = "Get configured school operating hours for partial-day leave")
    public TeacherPortalSchoolDayHoursDTO getSchoolDayHours() {
        return new TeacherPortalSchoolDayHoursDTO(
                schoolDayPolicyService.getSchoolStartTime(),
                schoolDayPolicyService.getSchoolEndTime()
        );
    }

    @PostMapping("/my-leave-requests")
    @Operation(summary = "Submit leave for the authenticated staff member")
    public TeacherLeaveRequestDTO submitLeave(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TeacherLeaveSaveRequest request
    ) {
        return leaveService.submit(principal.getUser().getId(), request);
    }

    @GetMapping("/my-leave-requests")
    @Operation(summary = "List leave submitted by the authenticated staff member")
    public Page<TeacherLeaveRequestDTO> getLeaveRequests(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) TeacherLeaveStatus status,
            Pageable pageable
    ) {
        // DEMO-FEATURE: teacher-my-leave-type-filter START
        // @RequestParam(required = false) TeacherLeaveType leaveType,
        // return leaveService.getOwnRequests(principal.getUser().getId(), status, leaveType, pageable);
        // DEMO-FEATURE: teacher-my-leave-type-filter END
        return leaveService.getOwnRequests(principal.getUser().getId(), status, pageable);
    }

    @GetMapping("/my-leave-requests/{requestId}")
    @Operation(summary = "Get an owned staff leave request")
    public TeacherLeaveRequestDTO getLeaveRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long requestId
    ) {
        return leaveService.getOwnRequest(principal.getUser().getId(), requestId);
    }

    @PatchMapping("/my-leave-requests/{requestId}")
    @Operation(summary = "Update an owned pending staff leave request")
    public TeacherLeaveRequestDTO updateLeave(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long requestId,
            @Valid @RequestBody TeacherLeaveSaveRequest request
    ) {
        return leaveService.updateOwnRequest(principal.getUser().getId(), requestId, request);
    }

    @PostMapping("/my-leave-requests/{requestId}/cancel")
    @Operation(summary = "Cancel an owned pending staff leave request")
    public TeacherLeaveRequestDTO cancelLeave(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long requestId
    ) {
        return leaveService.cancelOwnRequest(principal.getUser().getId(), requestId);
    }
}
