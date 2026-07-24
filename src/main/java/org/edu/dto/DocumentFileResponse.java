package org.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
public class DocumentFileResponse {

    private final String fileName;
    private final String contentType;
    private final long fileSize;
    private final Resource resource;
}
