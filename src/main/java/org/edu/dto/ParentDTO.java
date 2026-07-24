package org.edu.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParentDTO {

    private Long id;

    private Long userId;

    @Email(message = "Login email must be valid")
    private String loginEmail;

    @Size(min = 8, max = 100, message = "Login password must be between 8 and 100 characters")
    private String loginPassword;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(?:0\\d{9}|\\+94\\d{9})$",
            message = "Phone number must be valid (e.g., 0771234567 or +94771234567)"
    )
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Occupation is required")
    private String occupation;

    private boolean active;

    private List<Long> studentIds;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
