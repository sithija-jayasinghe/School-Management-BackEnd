package org.edu.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeachingAssignmentDTO {

    private Long id;

    @NotNull(message = "Teacher is required")
    private Long staffId;
    private String staffName;

    @NotNull(message = "Class is required")
    private Long classId;
    private String className;

    private Long gradeId;
    private String gradeName;

    @NotNull(message = "Subject is required")
    private Long subjectId;
    private String subjectCode;
    private String subjectName;

    @NotNull(message = "Academic year is required")
    private Long academicYearId;
    private String academicYearName;

    private boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
