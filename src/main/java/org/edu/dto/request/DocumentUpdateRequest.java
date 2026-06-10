package org.edu.dto.request;

import jakarta.validation.constraints.Pattern;
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
public class DocumentUpdateRequest {

    private DocumentType documentType;

    @Pattern(regexp = ".*\\S.*", message = "Document title must not be blank")
    @Size(max = 200, message = "Document title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Document description must not exceed 2000 characters")
    private String description;
    private Boolean visibleToParent;
}
