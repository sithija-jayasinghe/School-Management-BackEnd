package org.edu.dto.teacherleave;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.edu.util.TeacherLeaveCoverageStatus;

@Getter
@AllArgsConstructor
public class TeacherLeaveSessionDTO {
    private Long id;
    private Long timetableId;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long classId;
    private String className;
    private Long subjectId;
    private String subjectName;
    private String roomNumber;
    private Long substituteStaffId;
    private String substituteTeacherName;
    private TeacherLeaveCoverageStatus coverageStatus;
    private String coverageRemarks;
}
