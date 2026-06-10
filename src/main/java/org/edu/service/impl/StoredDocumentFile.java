package org.edu.service.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoredDocumentFile {

    private final String originalFileName;
    private final String storedFileName;
    private final String contentType;
    private final long fileSize;
}
