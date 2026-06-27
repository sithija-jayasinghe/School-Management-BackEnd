package org.edu.dto;

import jakarta.validation.constraints.NotBlank;
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
public class ClassDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private Long gradeId;

    private String gradeName;

    private Integer gradeLevel;

    private String section;

    private Long classTeacherId;

    private String classTeacherName;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<Long> subjectIds;
}
