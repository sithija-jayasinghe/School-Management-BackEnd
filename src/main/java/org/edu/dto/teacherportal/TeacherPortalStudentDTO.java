package org.edu.dto.teacherportal;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherPortalStudentDTO {

    private Long studentId;
    private String name;
    private LocalDate dateOfBirth;
    private String house;
    private boolean active;
    private Long classId;
    private String className;
}
