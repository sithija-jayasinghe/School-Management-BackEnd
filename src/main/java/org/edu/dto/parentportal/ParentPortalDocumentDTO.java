package org.edu.dto.parentportal;

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
public class ParentPortalDocumentDTO {

    private Long id;
    private Long studentId;
    private String title;
    private String description;
    private DocumentType documentType;
    private String originalFileName;
    private String contentType;
    private long fileSize;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
