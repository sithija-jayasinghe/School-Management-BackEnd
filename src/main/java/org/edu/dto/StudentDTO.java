package org.edu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Date of birth is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private boolean active;

    @NotBlank(message = "House is required")
    private String house;

    private Long currentClassId;

    private String currentClassName;

    private Long currentAcademicYearId;

    private String currentAcademicYearName;

    private List<Long> parentIds;

    @Valid
    private List<StudentParentInlineRequest> newParents;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
