package org.edu.dto.parentportal;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.AttendanceStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentPortalAttendanceDTO {

    private Long attendanceId;
    private LocalDate attendanceDate;
    private AttendanceStatus status;
    private String className;
    private String subjectName;
    private String teacherName;
    private String remarks;
}
