package org.edu.dto.teacherportal;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.DocumentType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherPortalDocumentDTO {

    private Long id;
    private Long studentId;
    private String studentName;
    private DocumentType documentType;
    private String title;
    private String description;
    private String originalFileName;
    private String contentType;
    private long fileSize;
    private boolean visibleToParent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
