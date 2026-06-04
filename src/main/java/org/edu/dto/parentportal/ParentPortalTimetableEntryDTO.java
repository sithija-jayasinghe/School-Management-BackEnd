package org.edu.dto.parentportal;

import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentPortalTimetableEntryDTO {

    private Long timetableId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String roomNumber;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Long staffId;
    private String teacherName;
}
