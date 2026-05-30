package org.edu.dto.parentportal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentPortalSubjectDTO {

    private Long subjectId;
    private String code;
    private String name;
    private String description;
}
