package org.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.NoticeAudience;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDTO {

    private Long id;

    @NotBlank(message = "Notice title is required")
    private String title;

    @NotBlank(message = "Notice message is required")
    private String message;

    @NotNull(message = "Notice audience is required")
    private NoticeAudience audience;

    private Long classId;
    private String className;
    private boolean published;
    private LocalDate publishDate;
    private LocalDate expiryDate;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
