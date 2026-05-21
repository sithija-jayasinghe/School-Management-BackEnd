package org.edu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimetableDTO {

    private Long id;

    @NotNull(message = "Class ID is required")
    private Long classId;
    private String className; // read-only

    @NotNull(message = "Subject ID is required")
    private Long subjectId;
    private String subjectName; // read-only

    @NotNull(message = "Staff ID is required")
    private Long staffId;
    private String staffName; // read-only

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private String roomNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
