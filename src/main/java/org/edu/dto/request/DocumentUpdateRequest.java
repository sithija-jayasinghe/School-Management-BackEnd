package org.edu.dto.request;

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
    private String title;
    private String description;
    private Boolean visibleToParent;
}
