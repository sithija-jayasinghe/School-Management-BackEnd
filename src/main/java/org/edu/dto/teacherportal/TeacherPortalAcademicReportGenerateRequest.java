package org.edu.dto.teacherportal;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherPortalAcademicReportGenerateRequest {

    @NotNull(message = "Academic term ID is required")
    private Long academicTermId;

    private String classTeacherRemarks;
    private String principalRemarks;
}
