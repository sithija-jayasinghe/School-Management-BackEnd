package org.edu.dto.teacherleave;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubstituteTeacherOptionDTO {
    private Long staffId;
    private String staffCode;
    private String name;
    private String designation;
    private boolean preferredForSubject;
    private boolean preferredForClass;
}
