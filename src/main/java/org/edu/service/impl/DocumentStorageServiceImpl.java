package org.edu.service.impl;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.edu.exception.DocumentStorageException;
import org.edu.service.DocumentStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentStorageServiceImpl implements DocumentStorageService {

    private final Path storageRoot;

    public DocumentStorageServiceImpl(@Value("${app.documents.storage-dir:uploads/documents}") String storageDir) {
        this.storageRoot = Paths.get(storageDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void initialize() {
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException ex) {
            throw new DocumentStorageException("Failed to initialize document storage", ex);
        }
    }

    @Override
    public StoredDocumentFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Document file is required");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());
        validateFileName(originalFileName);
        String extension = "";
        int extensionIndex = originalFileName.lastIndexOf('.');
        if (extensionIndex >= 0) {
            extension = originalFileName.substring(extensionIndex);
        }
        String storedFileName = UUID.randomUUID() + extension;

        try {
            Files.copy(file.getInputStream(), storageRoot.resolve(storedFileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new DocumentStorageException("Failed to store document file", ex);
        }

        return new StoredDocumentFile(
                originalFileName,
                storedFileName,
                file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
                file.getSize()
        );
    }

    @Override
    public Resource loadAsResource(String storedFileName) {
        try {
            validateFileName(storedFileName);
            Path filePath = storageRoot.resolve(storedFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new DocumentStorageException("Document file is not available for download");
            }
            return resource;
        } catch (IOException ex) {
            throw new DocumentStorageException("Failed to load document file", ex);
        }
    }

    @Override
    public void delete(String storedFileName) {
        try {
            validateFileName(storedFileName);
            Files.deleteIfExists(storageRoot.resolve(storedFileName).normalize());
        } catch (IOException ex) {
            throw new DocumentStorageException("Failed to delete stored document file", ex);
        }
    }

    private void validateFileName(String fileName) {
        if (!StringUtils.hasText(fileName) || fileName.contains("..")) {
            throw new DocumentStorageException("Invalid document file name");
        }
    }
}
