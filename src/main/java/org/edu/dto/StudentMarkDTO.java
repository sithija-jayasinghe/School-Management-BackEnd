package org.edu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentMarkDTO {

    private Long id;

    @NotNull(message = "Exam ID is required")
    private Long examId;
    private String examName;

    @NotNull(message = "Student ID is required")
    private Long studentId;
    private String studentName;

    private Long classId;
    private String className;

    private Long subjectId;
    private String subjectName;

    private Long enteredByStaffId;
    private String enteredByStaffName;

    @NotNull(message = "Marks obtained is required")
    @DecimalMin(value = "0.00", message = "Marks obtained cannot be negative")
    private BigDecimal marksObtained;

    private BigDecimal maxMarks;
    private BigDecimal percentage;
    private String grade;
    private boolean passed;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
