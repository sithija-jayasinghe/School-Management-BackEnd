package org.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    // DEMO-FEATURE: parent-nic-class-status-filters START
    @Pattern(
            regexp = "^(?:\\d{9}[vVxX]|\\d{12})$",
            message = "Parent NIC must be valid Sri Lankan NIC format"
    )
    private String nic;
    // DEMO-FEATURE: parent-nic-class-status-filters END

    @NotBlank(message = "Parent email is required")
    @Email(message = "Parent email must be valid")
    private String email;

    @NotBlank(message = "Parent password is required")
    @Size(min = 8, max = 100, message = "Parent password must be between 8 and 100 characters")
    private String password;

    @NotBlank(message = "Parent address is required")
    private String address;

    @NotBlank(message = "Parent occupation is required")
    private String occupation;
}
