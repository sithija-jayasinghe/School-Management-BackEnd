package org.edu.dto.teacherportal;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherPortalLeaveReviewRequest {

    @Size(max = 1000, message = "Reviewer remarks must not exceed 1000 characters")
    private String reviewerRemarks;
}
