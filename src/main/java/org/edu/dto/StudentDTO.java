package org.edu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Date of birth is required")
    @JsonFormat(pattern = "yyyy-mm-dd")
    private LocalDate dateOfBirth;

    private boolean active;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(?:0\\d{9}|\\+94\\d{9})$",
            message = "Phone number must be valid (e.g., 0771234567 or +94771234567)"
    )
    private String phoneNumber;

    private Long currentClassId;

    private String currentClassName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
