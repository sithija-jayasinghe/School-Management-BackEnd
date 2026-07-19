package org.edu.dto.activity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.edu.util.ActivityTeacherRole;

@Getter
@AllArgsConstructor
public class ActivityTeacherAssignmentDTO {
    private Long id;
    private Long activityId;
    private String activityCode;
    private String activityName;
    private Long staffId;
    private String staffCode;
    private String staffName;
    private String staffDesignation;
    private Long academicYearId;
    private String academicYearName;
    private ActivityTeacherRole responsibilityRole;
    private boolean primaryResponsible;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
