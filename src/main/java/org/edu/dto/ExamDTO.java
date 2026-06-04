package org.edu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.ExamType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamDTO {

    private Long id;

    @NotNull(message = "Academic year ID is required")
    private Long academicYearId;
    private String academicYearName;

    @NotNull(message = "Academic term ID is required")
    private Long academicTermId;
    private String academicTermName;

    @NotNull(message = "Class ID is required")
    private Long classId;
    private String className;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;
    private String subjectName;

    @NotBlank(message = "Exam name is required")
    private String name;

    @NotNull(message = "Exam type is required")
    private ExamType type;

    @NotNull(message = "Exam date is required")
    private LocalDate examDate;

    @NotNull(message = "Max marks is required")
    @DecimalMin(value = "1.00", message = "Max marks must be greater than zero")
    private BigDecimal maxMarks;

    @NotNull(message = "Pass marks is required")
    @DecimalMin(value = "0.00", message = "Pass marks cannot be negative")
    private BigDecimal passMarks;

    private boolean active;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
