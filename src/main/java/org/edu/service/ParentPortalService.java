package org.edu.service;

import java.util.List;
import org.edu.dto.parentportal.ParentPortalDashboardDTO;
import org.edu.dto.parentportal.ParentPortalProfileDTO;
import org.edu.dto.parentportal.ParentPortalStudentDetailDTO;
import org.edu.dto.parentportal.ParentPortalStudentSummaryDTO;
import org.edu.dto.parentportal.ParentPortalSubjectDTO;
import org.edu.dto.parentportal.ParentPortalTimetableEntryDTO;

public interface ParentPortalService {

    ParentPortalProfileDTO getProfile(Long authenticatedUserId);

    ParentPortalDashboardDTO getDashboard(Long authenticatedUserId);

    List<ParentPortalStudentSummaryDTO> getLinkedStudents(Long authenticatedUserId);

    ParentPortalStudentDetailDTO getStudentDetail(Long authenticatedUserId, Long studentId);

    List<ParentPortalSubjectDTO> getStudentSubjects(Long authenticatedUserId, Long studentId);

    List<ParentPortalTimetableEntryDTO> getStudentTimetable(Long authenticatedUserId, Long studentId);
}
