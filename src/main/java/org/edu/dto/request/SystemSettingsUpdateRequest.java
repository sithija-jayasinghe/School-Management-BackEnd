package org.edu.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SystemSettingsUpdateRequest {

    @Pattern(regexp = "^$|.*\\S.*", message = "School name must not be blank")
    @Size(max = 180, message = "School name must not exceed 180 characters")
    private String schoolName;

    @Pattern(regexp = "^$|.*\\S.*", message = "School code must not be blank")
    @Size(max = 50, message = "School code must not exceed 50 characters")
    private String schoolCode;

    @Pattern(regexp = "^$|.*\\S.*", message = "Address must not be blank")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Pattern(
            regexp = "^(?:$|0\\d{9}|\\+94\\d{9})$",
            message = "Phone number must be valid (e.g., 0771234567 or +94771234567)"
    )
    private String phoneNumber;

    @Email(message = "Email must be valid")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Pattern(regexp = "^$|.*\\S.*", message = "Principal name must not be blank")
    @Size(max = 120, message = "Principal name must not exceed 120 characters")
    private String principalName;

    @Pattern(regexp = "^$|.*\\S.*", message = "Principal title must not be blank")
    @Size(max = 120, message = "Principal title must not exceed 120 characters")
    private String principalTitle;

    private LocalTime schoolStartTime;
    private LocalTime schoolEndTime;
    private LocalTime attendanceCutoffTime;

    @Pattern(regexp = "^$|.*\\S.*", message = "Default language must not be blank")
    @Size(max = 10, message = "Default language must not exceed 10 characters")
    private String defaultLanguage;

    @Pattern(regexp = "^$|.*\\S.*", message = "Time zone must not be blank")
    @Size(max = 50, message = "Time zone must not exceed 50 characters")
    private String timeZone;

}
