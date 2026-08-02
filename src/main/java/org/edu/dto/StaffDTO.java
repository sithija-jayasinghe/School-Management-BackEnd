package org.edu.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalDate;
import org.edu.util.EmploymentType;
import org.edu.util.Role;
import org.edu.util.StaffCategory;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StaffDTO {

    private Long id;

    private Long userId;

    @Email(message = "Login email must be valid")
    private String loginEmail;

    @Size(min = 8, max = 100, message = "Login password must be between 8 and 100 characters")
    private String loginPassword;

    private Role loginRole;

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

    // DEMO-FEATURE: staff-phone-nic-columns START
    // Purpose: Allows optional Sri Lankan NIC in Staff API responses/requests.
    @Pattern(
            regexp = "^(?:\\d{9}[vVxX]|\\d{12})$",
            message = "NIC must be valid Sri Lankan NIC format"
    )
    private String nic;
    // DEMO-FEATURE: staff-phone-nic-columns END

    @NotBlank(message = "Designation is required")
    private String designation;

    private StaffCategory staffCategory;

    private EmploymentType employmentType;

    private String department;

    private LocalDate joiningDate;

    private Boolean teachingCapable;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
