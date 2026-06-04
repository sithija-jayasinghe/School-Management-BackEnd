package org.edu.dto.parentportal;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentPortalProfileDTO {

    private Long parentId;
    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String occupation;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
