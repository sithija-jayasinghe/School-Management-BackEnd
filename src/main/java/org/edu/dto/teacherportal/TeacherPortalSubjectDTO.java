package org.edu.dto.teacherportal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherPortalSubjectDTO {

    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String description;
    private int assignedClassCount;
    private int weeklySessionCount;
}
