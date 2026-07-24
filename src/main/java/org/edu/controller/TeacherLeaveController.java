package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.teacherleave.SubstituteTeacherOptionDTO;
import org.edu.dto.teacherleave.TeacherLeaveCoverageRequest;
import org.edu.dto.teacherleave.TeacherLeaveRequestDTO;
import org.edu.dto.teacherleave.TeacherLeaveReviewRequest;
import org.edu.dto.teacherleave.TeacherLeaveSessionDTO;
import org.edu.security.UserPrincipal;
import org.edu.service.TeacherLeaveService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher-leave-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Teacher Leave Administration", description = "Review teacher leave and arrange date-specific lesson coverage")
@SecurityRequirement(name = "bearerAuth")
public class TeacherLeaveController {

    private final TeacherLeaveService teacherLeaveService;

    @GetMapping
    @Operation(summary = "List teacher leave requests")
    public Page<TeacherLeaveRequestDTO> getRequests(
            @RequestParam(required = false) TeacherLeaveStatus status,
            Pageable pageable
    ) {
        return teacherLeaveService.getAllRequests(status, pageable);
    }

    @GetMapping("/{requestId}")
    @Operation(summary = "Get a teacher leave request and coverage summary")
    public TeacherLeaveRequestDTO getRequest(@PathVariable Long requestId) {
        return teacherLeaveService.getRequest(requestId);
    }

    @GetMapping("/{requestId}/sessions")
    @Operation(summary = "List the lessons affected by a teacher leave request")
    public List<TeacherLeaveSessionDTO> getAffectedSessions(@PathVariable Long requestId) {
        return teacherLeaveService.getAffectedSessions(requestId);
    }

    @GetMapping("/sessions/{sessionId}/substitute-options")
    @Operation(summary = "List available substitute teachers for an affected lesson")
    public List<SubstituteTeacherOptionDTO> getSubstituteOptions(@PathVariable Long sessionId) {
        return teacherLeaveService.getSubstituteOptions(sessionId);
    }

    @PatchMapping("/sessions/{sessionId}/coverage")
    @Operation(summary = "Assign, clear, or waive lesson coverage")
    public TeacherLeaveSessionDTO updateCoverage(
            @PathVariable Long sessionId,
            @Valid @RequestBody TeacherLeaveCoverageRequest request
    ) {
        return teacherLeaveService.updateCoverage(sessionId, request);
    }

    @PostMapping("/{requestId}/approve")
    @Operation(summary = "Approve teacher leave after all lessons have a coverage decision")
    public TeacherLeaveRequestDTO approve(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long requestId,
            @Valid @RequestBody TeacherLeaveReviewRequest request
    ) {
        return teacherLeaveService.approve(principal.getUser().getId(), requestId, request);
    }

    @PostMapping("/{requestId}/reject")
    @Operation(summary = "Reject a pending teacher leave request")
    public TeacherLeaveRequestDTO reject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long requestId,
            @Valid @RequestBody TeacherLeaveReviewRequest request
    ) {
        return teacherLeaveService.reject(principal.getUser().getId(), requestId, request);
    }
}
