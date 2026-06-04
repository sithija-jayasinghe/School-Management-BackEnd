package org.edu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.AttendanceStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkAttendanceStudentRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;

    private String remarks;
}
