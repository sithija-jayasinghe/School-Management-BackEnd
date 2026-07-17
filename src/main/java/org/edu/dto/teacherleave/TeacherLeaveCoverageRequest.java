package org.edu.dto.teacherleave;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.TeacherLeaveCoverageStatus;

@Getter
@Setter
@NoArgsConstructor
public class TeacherLeaveCoverageRequest {

    @NotNull(message = "Coverage status is required")
    private TeacherLeaveCoverageStatus coverageStatus;

    private Long substituteStaffId;

    @Size(max = 500, message = "Coverage remarks must not exceed 500 characters")
    private String remarks;
}
