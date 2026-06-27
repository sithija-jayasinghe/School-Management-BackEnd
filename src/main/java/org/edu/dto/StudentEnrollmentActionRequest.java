package org.edu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentEnrollmentActionRequest {

    @NotNull(message = "Class ID is required")
    private Long classId;
}
