package org.edu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestReviewRequest {

    @NotNull(message = "Reviewed by staff ID is required")
    private Long reviewedByStaffId;

    private String reviewerRemarks;
}
