package org.edu.service;

import org.edu.service.impl.StoredDocumentFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentStorageService {

    StoredDocumentFile store(MultipartFile file);

    Resource loadAsResource(String storedFileName);

    void delete(String storedFileName);
}
