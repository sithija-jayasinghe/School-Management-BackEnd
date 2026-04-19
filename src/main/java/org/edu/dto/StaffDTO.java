package org.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StaffDTO {

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Staff ID is required")
    private String staffId;

    @NotBlank(message = "Name is required")
    private String name;

    private boolean active;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(?:0\\d{9}|\\+94\\d{9})$",
            message = "Phone number must be valid (e.g., 0771234567 or +94771234567)"
    )
    private String phoneNumber;

    @NotBlank(message = "Designation is required")
    private String designation;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
