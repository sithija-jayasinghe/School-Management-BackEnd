package org.edu.dto.activity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.edu.util.ActivityCategory;

@Getter
@AllArgsConstructor
public class ActivityDTO {
    private Long id;
    private String code;
    private String name;
    private ActivityCategory category;
    private String description;
    private String venue;
    private boolean active;
    private long responsibleTeacherCount;
    private String primaryTeacherName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
