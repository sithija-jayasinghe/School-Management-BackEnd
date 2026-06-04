package org.edu.dto.parentportal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentPortalStudentDetailDTO {

    private Long studentId;
    private String name;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private boolean active;
    private Long classId;
    private String className;
    private String classTeacherName;
    private String relationshipType;
    private boolean primaryContact;
    private boolean emergencyContact;
    private List<ParentPortalSubjectDTO> subjects;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
