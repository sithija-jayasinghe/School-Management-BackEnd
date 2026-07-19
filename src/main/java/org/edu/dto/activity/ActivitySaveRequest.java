package org.edu.dto.activity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.ActivityCategory;

@Getter
@Setter
@NoArgsConstructor
public class ActivitySaveRequest {

    @NotBlank(message = "Activity code is required")
    @Pattern(regexp = "[A-Za-z0-9_-]+", message = "Activity code may contain letters, numbers, hyphens, and underscores")
    @Size(max = 30, message = "Activity code must not exceed 30 characters")
    private String code;

    @NotBlank(message = "Activity name is required")
    @Size(max = 150, message = "Activity name must not exceed 150 characters")
    private String name;

    @NotNull(message = "Activity category is required")
    private ActivityCategory category;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 150, message = "Venue must not exceed 150 characters")
    private String venue;
}
