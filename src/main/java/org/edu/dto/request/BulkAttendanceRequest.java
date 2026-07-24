package org.edu.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkAttendanceRequest {

    @NotNull(message = "Class ID is required")
    @Positive(message = "Class ID must be positive")
    private Long classId;

    @Positive(message = "Subject ID must be positive")
    private Long subjectId;

    @Positive(message = "Timetable ID must be positive")
    private Long timetableId;

    @Positive(message = "Staff ID must be positive")
    private Long markedByStaffId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @Valid
    @NotEmpty(message = "At least one student attendance record is required")
    private List<BulkAttendanceStudentRequest> students;
}
