package org.edu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.DocumentType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCreateRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "Document title is required")
    private String title;

    private String description;

    private boolean visibleToParent;
}
