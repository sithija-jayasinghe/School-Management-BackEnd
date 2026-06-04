package org.edu.dto.parentportal;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.ExamType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentPortalResultDTO {

    private Long markId;
    private Long examId;
    private String examName;
    private ExamType examType;
    private LocalDate examDate;
    private String academicTermName;
    private String subjectName;
    private BigDecimal marksObtained;
    private BigDecimal maxMarks;
    private BigDecimal percentage;
    private String grade;
    private boolean passed;
    private String remarks;
}
