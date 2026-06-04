package org.edu.dto.parentportal;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentPortalDashboardDTO {

    private ParentPortalProfileDTO profile;
    private int linkedStudentCount;
    private List<ParentPortalStudentSummaryDTO> students;
}
