package org.edu.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademicReportSubjectDTO {

    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private int examCount;
    private BigDecimal totalMarksObtained;
    private BigDecimal totalMaxMarks;
    private BigDecimal percentage;
    private String grade;
    private boolean passed;
}
