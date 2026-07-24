package org.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HouseDTO {

    private Long id;

    @NotBlank(message = "House name is required")
    private String name;

    @NotBlank(message = "House colour is required")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "House colour must be a hex colour like #2563EB")
    private String colour;

    private boolean active;

    private long studentCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
