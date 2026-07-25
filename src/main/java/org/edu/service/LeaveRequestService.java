package org.edu.service;

import org.edu.dto.LeaveRequestDTO;
import org.edu.dto.request.LeaveRequestReviewRequest;
import org.edu.dto.parentportal.ParentPortalLeaveRequestCreateDTO;
import org.edu.util.LeaveRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeaveRequestService {

    LeaveRequestDTO createLeaveRequest(LeaveRequestDTO dto);

    LeaveRequestDTO updateLeaveRequest(Long id, LeaveRequestDTO dto);

    LeaveRequestDTO approveLeaveRequest(Long authenticatedUserId, Long id, LeaveRequestReviewRequest request);

    LeaveRequestDTO rejectLeaveRequest(Long authenticatedUserId, Long id, LeaveRequestReviewRequest request);

    LeaveRequestDTO cancelLeaveRequest(Long id, String reviewerRemarks);

    LeaveRequestDTO getLeaveRequestById(Long id);

    Page<LeaveRequestDTO> getAllLeaveRequests(Long authenticatedUserId, Pageable pageable);

    Page<LeaveRequestDTO> getLeaveRequestsByStatus(Long authenticatedUserId, LeaveRequestStatus status, Pageable pageable);

    Page<LeaveRequestDTO> getLeaveRequestsByStudent(Long studentId, Pageable pageable);

    Page<LeaveRequestDTO> getLeaveRequestsByParent(Long parentId, Pageable pageable);

    LeaveRequestDTO createParentLeaveRequest(Long authenticatedUserId, ParentPortalLeaveRequestCreateDTO dto);

    Page<LeaveRequestDTO> getParentLeaveRequests(Long authenticatedUserId, Pageable pageable);

    LeaveRequestDTO cancelParentLeaveRequest(Long authenticatedUserId, Long leaveRequestId, String remarks);

    Page<LeaveRequestDTO> getTeacherLeaveRequests(Long authenticatedUserId, LeaveRequestStatus status, Pageable pageable);

    LeaveRequestDTO approveTeacherLeaveRequest(Long authenticatedUserId, Long leaveRequestId, String reviewerRemarks);

    LeaveRequestDTO rejectTeacherLeaveRequest(Long authenticatedUserId, Long leaveRequestId, String reviewerRemarks);

    LeaveRequestDTO applyApprovedLeaveToAttendance(Long authenticatedUserId, Long leaveRequestId);
}
