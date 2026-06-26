package org.edu.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingsDTO {

    private Long id;
    private String schoolName;
    private String schoolCode;
    private String address;
    private String phoneNumber;
    private String email;
    private String principalName;
    private String principalTitle;
    private LocalTime schoolStartTime;
    private LocalTime schoolEndTime;
    private LocalTime attendanceCutoffTime;
    private String defaultLanguage;
    private String timeZone;
    private String currentAcademicYearLabel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
