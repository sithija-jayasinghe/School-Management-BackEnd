package org.edu.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestReviewRequest {

    @Positive(message = "Reviewed by staff ID must be positive")
    private Long reviewedByStaffId;

    @Size(max = 1000, message = "Reviewer remarks must not exceed 1000 characters")
    private String reviewerRemarks;
}
