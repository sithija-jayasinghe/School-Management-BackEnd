package org.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GradeDTO {

    private Long id;

    @NotBlank(message = "Grade name is required")
    private String name;

    @NotNull(message = "Grade level is required")
    private Integer level;

    private boolean active;
}
