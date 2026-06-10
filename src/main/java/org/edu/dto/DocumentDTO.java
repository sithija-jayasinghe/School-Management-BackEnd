package org.edu.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.DocumentType;
import org.edu.util.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long classId;
    private String className;
    private Long uploadedByUserId;
    private String uploadedByUserName;
    private Role uploadedByRole;
    private DocumentType documentType;
    private String title;
    private String description;
    private String originalFileName;
    private String contentType;
    private long fileSize;
    private boolean visibleToParent;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
