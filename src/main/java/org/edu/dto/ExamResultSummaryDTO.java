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
public class ExamResultSummaryDTO {

    private Long examId;
    private String examName;
    private Long classId;
    private String className;
    private Long subjectId;
    private String subjectName;
    private long totalStudentsMarked;
    private long passCount;
    private long failCount;
    private BigDecimal averageMarks;
    private BigDecimal highestMarks;
    private BigDecimal lowestMarks;
}
