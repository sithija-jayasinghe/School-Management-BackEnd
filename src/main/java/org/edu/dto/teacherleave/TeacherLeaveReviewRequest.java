package org.edu.dto.teacherleave;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TeacherLeaveReviewRequest {

    @NotBlank(message = "Reviewer remarks are required")
    @Size(max = 1000, message = "Reviewer remarks must not exceed 1000 characters")
    private String reviewerRemarks;
}
