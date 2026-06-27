package org.edu.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.EnrollmentStatus;

@Getter
@Setter
@NoArgsConstructor
public class StudentEnrollmentDTO {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long academicYearId;
    private String academicYearName;
    private Long classId;
    private String className;
    private LocalDate startDate;
    private LocalDate endDate;
    private EnrollmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
