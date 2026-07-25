package org.edu.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.ExamType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkExamCreateRequest {

    @NotNull(message = "Academic year is required")
    private Long academicYearId;

    @NotNull(message = "Academic term is required")
    private Long academicTermId;

    @NotNull(message = "Class is required")
    private Long classId;

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

    private String description;

    @NotEmpty(message = "At least one subject is required")
    private List<Long> subjectIds;
}
