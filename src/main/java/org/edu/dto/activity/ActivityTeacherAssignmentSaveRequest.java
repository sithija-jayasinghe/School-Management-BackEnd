package org.edu.dto.activity;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.ActivityTeacherRole;

@Getter
@Setter
@NoArgsConstructor
public class ActivityTeacherAssignmentSaveRequest {

    @NotNull(message = "Teacher is required")
    private Long staffId;

    @NotNull(message = "Academic year is required")
    private Long academicYearId;

    @NotNull(message = "Responsibility role is required")
    private ActivityTeacherRole responsibilityRole;

    private boolean primaryResponsible;
}
