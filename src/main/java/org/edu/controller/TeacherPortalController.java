package org.edu.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.teacherportal.TeacherPortalClassSummaryDTO;
import org.edu.dto.teacherportal.TeacherPortalDashboardDTO;
import org.edu.dto.teacherportal.TeacherPortalProfileDTO;
import org.edu.dto.teacherportal.TeacherPortalTimetableEntryDTO;
import org.edu.security.UserPrincipal;
import org.edu.service.TeacherPortalService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher-portal")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherPortalController {

    private final TeacherPortalService teacherPortalService;

    @GetMapping("/profile")
    public TeacherPortalProfileDTO getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getProfile(principal.getUser().getId());
    }

    @GetMapping("/dashboard")
    public TeacherPortalDashboardDTO getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getDashboard(principal.getUser().getId());
    }

    @GetMapping("/classes")
    public List<TeacherPortalClassSummaryDTO> getAssignedClasses(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getAssignedClasses(principal.getUser().getId());
    }

    @GetMapping("/schedule")
    public List<TeacherPortalTimetableEntryDTO> getSchedule(@AuthenticationPrincipal UserPrincipal principal) {
        return teacherPortalService.getSchedule(principal.getUser().getId());
    }
}
