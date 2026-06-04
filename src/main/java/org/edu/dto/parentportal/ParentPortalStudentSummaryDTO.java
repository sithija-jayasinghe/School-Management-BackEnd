package org.edu.dto.parentportal;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentPortalStudentSummaryDTO {

    private Long studentId;
    private String name;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private Long classId;
    private String className;
    private String relationshipType;
    private boolean primaryContact;
    private boolean emergencyContact;
}
