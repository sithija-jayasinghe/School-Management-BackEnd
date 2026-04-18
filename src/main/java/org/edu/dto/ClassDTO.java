package org.edu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClassDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
