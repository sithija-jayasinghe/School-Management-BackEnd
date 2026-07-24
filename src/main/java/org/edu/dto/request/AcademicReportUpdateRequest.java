package org.edu.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademicReportUpdateRequest {

    @Size(max = 2000, message = "Class teacher remarks must not exceed 2000 characters")
    private String classTeacherRemarks;

    @Size(max = 2000, message = "Principal remarks must not exceed 2000 characters")
    private String principalRemarks;
}
