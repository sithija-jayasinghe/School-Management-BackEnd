package org.edu.dto.teacherportal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.DocumentType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherPortalDocumentCreateRequest {

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "Document title is required")
    @Size(max = 200, message = "Document title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Document description must not exceed 2000 characters")
    private String description;

    private boolean visibleToParent;
}
