package org.edu.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademicReportGenerateRequest {

    @NotNull(message = "Student ID is required")
    @Positive(message = "Student ID must be positive")
    private Long studentId;

    @NotNull(message = "Academic term ID is required")
    @Positive(message = "Academic term ID must be positive")
    private Long academicTermId;

    @Size(max = 2000, message = "Class teacher remarks must not exceed 2000 characters")
    private String classTeacherRemarks;

    @Size(max = 2000, message = "Principal remarks must not exceed 2000 characters")
    private String principalRemarks;
}
