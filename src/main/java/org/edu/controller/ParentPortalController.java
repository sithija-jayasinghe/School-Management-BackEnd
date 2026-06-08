package org.edu.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.edu.dto.LeaveRequestDTO;
import lombok.RequiredArgsConstructor;
import org.edu.dto.parentportal.ParentPortalAttendanceDTO;
import org.edu.dto.parentportal.ParentPortalDashboardDTO;
import org.edu.dto.parentportal.ParentPortalLeaveRequestCreateDTO;
import org.edu.dto.parentportal.ParentPortalNoticeDTO;
import org.edu.dto.parentportal.ParentPortalProfileDTO;
import org.edu.dto.parentportal.ParentPortalResultDTO;
import org.edu.dto.parentportal.ParentPortalStudentDetailDTO;
import org.edu.dto.parentportal.ParentPortalStudentSummaryDTO;
import org.edu.dto.parentportal.ParentPortalSubjectDTO;
import org.edu.dto.parentportal.ParentPortalTimetableEntryDTO;
import org.edu.security.UserPrincipal;
import org.edu.service.ParentPortalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parent-portal")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARENT')")
public class ParentPortalController {

    private final ParentPortalService parentPortalService;

    @GetMapping("/profile")
    public ParentPortalProfileDTO getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return parentPortalService.getProfile(principal.getUser().getId());
    }

    @GetMapping("/dashboard")
    public ParentPortalDashboardDTO getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return parentPortalService.getDashboard(principal.getUser().getId());
    }

    @GetMapping("/notices")
    public List<ParentPortalNoticeDTO> getNotices(@AuthenticationPrincipal UserPrincipal principal) {
        return parentPortalService.getNotices(principal.getUser().getId());
    }

    @GetMapping("/students")
    public List<ParentPortalStudentSummaryDTO> getLinkedStudents(@AuthenticationPrincipal UserPrincipal principal) {
        return parentPortalService.getLinkedStudents(principal.getUser().getId());
    }

    @GetMapping("/students/{studentId}")
    public ParentPortalStudentDetailDTO getStudentDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId
    ) {
        return parentPortalService.getStudentDetail(principal.getUser().getId(), studentId);
    }

    @GetMapping("/students/{studentId}/subjects")
    public List<ParentPortalSubjectDTO> getStudentSubjects(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId
    ) {
        return parentPortalService.getStudentSubjects(principal.getUser().getId(), studentId);
    }

    @GetMapping("/students/{studentId}/timetable")
    public List<ParentPortalTimetableEntryDTO> getStudentTimetable(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId
    ) {
        return parentPortalService.getStudentTimetable(principal.getUser().getId(), studentId);
    }

    @GetMapping("/students/{studentId}/attendance")
    public List<ParentPortalAttendanceDTO> getStudentAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return parentPortalService.getStudentAttendance(principal.getUser().getId(), studentId, from, to);
    }

    @GetMapping("/students/{studentId}/results")
    public List<ParentPortalResultDTO> getStudentResults(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long studentId
    ) {
        return parentPortalService.getStudentResults(principal.getUser().getId(), studentId);
    }

    @PostMapping("/leave-requests")
    public LeaveRequestDTO createLeaveRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ParentPortalLeaveRequestCreateDTO dto
    ) {
        return parentPortalService.createLeaveRequest(principal.getUser().getId(), dto);
    }

    @GetMapping("/leave-requests")
    public Page<LeaveRequestDTO> getLeaveRequests(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable
    ) {
        return parentPortalService.getLeaveRequests(principal.getUser().getId(), pageable);
    }

    @PostMapping("/leave-requests/{leaveRequestId}/cancel")
    public LeaveRequestDTO cancelLeaveRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long leaveRequestId,
            @RequestParam(required = false) String remarks
    ) {
        return parentPortalService.cancelLeaveRequest(principal.getUser().getId(), leaveRequestId, remarks);
    }
}
