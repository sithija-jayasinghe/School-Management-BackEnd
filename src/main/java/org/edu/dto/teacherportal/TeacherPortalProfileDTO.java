package org.edu.dto.teacherportal;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherPortalProfileDTO {

    private Long staffId;
    private Long userId;
    private String staffCode;
    private String name;
    private String email;
    private String phoneNumber;
    private String designation;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
