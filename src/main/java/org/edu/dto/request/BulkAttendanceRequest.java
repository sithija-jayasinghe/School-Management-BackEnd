package org.edu.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    private Long classId;

    private Long subjectId;

    private Long timetableId;

    private Long markedByStaffId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @Valid
    @NotEmpty(message = "At least one student attendance record is required")
    private List<BulkAttendanceStudentRequest> students;
}
