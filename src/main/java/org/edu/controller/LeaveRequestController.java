package org.edu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.dto.LeaveRequestDTO;
import org.edu.dto.request.LeaveRequestReviewRequest;
import org.edu.service.LeaveRequestService;
import org.edu.util.LeaveRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping
    public LeaveRequestDTO createLeaveRequest(@Valid @RequestBody LeaveRequestDTO dto) {
        return leaveRequestService.createLeaveRequest(dto);
    }

    @PatchMapping("/{id}")
    public LeaveRequestDTO updateLeaveRequest(@PathVariable Long id, @RequestBody LeaveRequestDTO dto) {
        return leaveRequestService.updateLeaveRequest(id, dto);
    }

    @PostMapping("/{id}/approve")
    public LeaveRequestDTO approveLeaveRequest(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequestReviewRequest request
    ) {
        return leaveRequestService.approveLeaveRequest(id, request);
    }

    @PostMapping("/{id}/reject")
    public LeaveRequestDTO rejectLeaveRequest(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequestReviewRequest request
    ) {
        return leaveRequestService.rejectLeaveRequest(id, request);
    }

    @PostMapping("/{id}/cancel")
    public LeaveRequestDTO cancelLeaveRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks
    ) {
        return leaveRequestService.cancelLeaveRequest(id, remarks);
    }

    @GetMapping
    public Page<LeaveRequestDTO> getAllLeaveRequests(Pageable pageable) {
        return leaveRequestService.getAllLeaveRequests(pageable);
    }

    @GetMapping("/{id}")
    public LeaveRequestDTO getLeaveRequestById(@PathVariable Long id) {
        return leaveRequestService.getLeaveRequestById(id);
    }

    @GetMapping("/status/{status}")
    public Page<LeaveRequestDTO> getLeaveRequestsByStatus(@PathVariable LeaveRequestStatus status, Pageable pageable) {
        return leaveRequestService.getLeaveRequestsByStatus(status, pageable);
    }

    @GetMapping("/students/{studentId}")
    public Page<LeaveRequestDTO> getLeaveRequestsByStudent(@PathVariable Long studentId, Pageable pageable) {
        return leaveRequestService.getLeaveRequestsByStudent(studentId, pageable);
    }

    @GetMapping("/parents/{parentId}")
    public Page<LeaveRequestDTO> getLeaveRequestsByParent(@PathVariable Long parentId, Pageable pageable) {
        return leaveRequestService.getLeaveRequestsByParent(parentId, pageable);
    }
}
