package org.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.LeaveRequestStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {

    private Long id;

    @NotNull(message = "Parent ID is required")
    private Long parentId;
    private String parentName;

    @NotNull(message = "Student ID is required")
    private Long studentId;
    private String studentName;

    private Long classId;
    private String className;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String note;

    private LeaveRequestStatus status;

    private Long reviewedByStaffId;
    private String reviewedByStaffName;
    private String reviewerRemarks;
    private LocalDateTime reviewedAt;
    private boolean attendanceApplied;
    private LocalDateTime attendanceAppliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
