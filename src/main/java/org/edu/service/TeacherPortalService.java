package org.edu.service;

import java.util.List;
import org.edu.dto.teacherportal.TeacherPortalClassSummaryDTO;
import org.edu.dto.teacherportal.TeacherPortalDashboardDTO;
import org.edu.dto.teacherportal.TeacherPortalProfileDTO;
import org.edu.dto.teacherportal.TeacherPortalTimetableEntryDTO;

public interface TeacherPortalService {

    TeacherPortalProfileDTO getProfile(Long authenticatedUserId);

    TeacherPortalDashboardDTO getDashboard(Long authenticatedUserId);

    List<TeacherPortalClassSummaryDTO> getAssignedClasses(Long authenticatedUserId);

    List<TeacherPortalTimetableEntryDTO> getSchedule(Long authenticatedUserId);
}
