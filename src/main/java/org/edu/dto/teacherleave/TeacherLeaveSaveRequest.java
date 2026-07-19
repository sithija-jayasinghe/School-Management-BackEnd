package org.edu.dto.teacherleave;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.TeacherLeaveDuration;
import org.edu.util.TeacherLeaveType;

@Getter
@Setter
@NoArgsConstructor
public class TeacherLeaveSaveRequest {

    @NotNull(message = "Leave type is required")
    private TeacherLeaveType leaveType;

    @NotNull(message = "Duration type is required")
    private TeacherLeaveDuration durationType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private LocalTime startTime;
    private LocalTime endTime;

    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
