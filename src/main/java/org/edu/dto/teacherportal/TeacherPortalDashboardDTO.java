package org.edu.dto.teacherportal;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherPortalDashboardDTO {

    private TeacherPortalProfileDTO profile;
    private int assignedClassCount;
    private int weeklySessionCount;
    private List<TeacherPortalClassSummaryDTO> assignedClasses;
    private List<TeacherPortalTimetableEntryDTO> schedule;
}
