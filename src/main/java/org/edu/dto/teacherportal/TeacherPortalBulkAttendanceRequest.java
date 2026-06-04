package org.edu.dto.teacherportal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.dto.request.BulkAttendanceStudentRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherPortalBulkAttendanceRequest {

    private Long subjectId;

    private Long timetableId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @Valid
    @NotEmpty(message = "At least one student attendance record is required")
    private List<BulkAttendanceStudentRequest> students;
}
