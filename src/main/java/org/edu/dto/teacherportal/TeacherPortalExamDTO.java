package org.edu.dto.teacherportal;

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
public class TeacherPortalExamDTO {

    private Long examId;
    private String examName;
    private ExamType examType;
    private LocalDate examDate;
    private String academicYearName;
    private String academicTermName;
    private Long classId;
    private String className;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private BigDecimal maxMarks;
    private BigDecimal passMarks;
    private boolean active;
}
