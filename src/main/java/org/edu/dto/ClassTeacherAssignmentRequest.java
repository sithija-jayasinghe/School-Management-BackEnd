package org.edu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassTeacherAssignmentRequest {

    @NotNull(message = "Teacher is required")
    private Long staffId;

    @NotNull(message = "Class is required")
    private Long classId;

    @NotNull(message = "Academic year is required")
    private Long academicYearId;

    @NotEmpty(message = "At least one subject is required")
    private List<Long> subjectIds;
}
