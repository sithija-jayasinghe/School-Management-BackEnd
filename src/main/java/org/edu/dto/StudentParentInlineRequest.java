package org.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StudentParentInlineRequest {

    @NotBlank(message = "Parent name is required")
    private String name;

    @NotBlank(message = "Parent phone number is required")
    @Pattern(
            regexp = "^(?:0\\d{9}|\\+94\\d{9})$",
            message = "Parent phone number must be valid (e.g., 0771234567 or +94771234567)"
    )
    private String phoneNumber;

    @NotBlank(message = "Parent address is required")
    private String address;

    @NotBlank(message = "Parent occupation is required")
    private String occupation;
}
