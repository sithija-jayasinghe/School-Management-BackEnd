package org.edu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademicReportUpdateRequest {

    private String classTeacherRemarks;
    private String principalRemarks;
}
