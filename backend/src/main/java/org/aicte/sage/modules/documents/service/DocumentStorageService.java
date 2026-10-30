package org.aicte.sage.modules.documents.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentStorageService {

    private static final Logger log = LoggerFactory.getLogger(DocumentStorageService.class);

    private static final long MAX_FILE_SIZE_BYTES = 15 * 1024 * 1024; // 15 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".png", ".jpg", ".jpeg");

    private final Path storageDirectory;

    public DocumentStorageService(@Value("${sage.documents.storage-dir:data/documents}") String storageDir) {
        this.storageDirectory = Paths.get(storageDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException e) {
            log.error("Failed to create document storage directory: {}", this.storageDirectory, e);
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    public record StoredFileMetadata(
            String originalFilename,
            String storedFilename,
            String filePath,
            String contentType,
            long fileSizeBytes,
            String sha256Hash
    ) {}

    public StoredFileMetadata storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty or null file");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds maximum allowed size of 15MB (actual: " + file.getSize() + " bytes)");
        }

        String rawOriginalFilename = file.getOriginalFilename();
        if (rawOriginalFilename == null || rawOriginalFilename.isBlank()) {
            rawOriginalFilename = "document_" + System.currentTimeMillis();
        }

        // 1. Sanitize original filename (defend against path traversal)
        String sanitizedOriginal = Paths.get(rawOriginalFilename).getFileName().toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");

        String extension = "";
        int dotIndex = sanitizedOriginal.lastIndexOf('.');
        if (dotIndex != -1) {
            extension = sanitizedOriginal.substring(dotIndex).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported file type '" + extension + "'. Allowed: " + ALLOWED_EXTENSIONS);
        }

        // 2. Validate Magic Bytes / File Signatures
        validateFileSignature(file, extension);

        // 3. Compute SHA-256 Hash
        String sha256Hash = computeSha256(file);

        // 4. Generate safe unique UUID filename on server filesystem
        String storedFilename = UUID.randomUUID() + extension;
        Path targetPath = this.storageDirectory.resolve(storedFilename).normalize();

        if (!targetPath.startsWith(this.storageDirectory)) {
            throw new SecurityException("Path traversal attempt detected!");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to persist file: {}", targetPath, e);
            throw new RuntimeException("Failed to store file on disk", e);
        }

        return new StoredFileMetadata(
                sanitizedOriginal,
                storedFilename,
                targetPath.toString(),
                file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                file.getSize(),
                sha256Hash
        );
    }

    private void validateFileSignature(MultipartFile file, String extension) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);
            if (read < 4) {
                throw new IllegalArgumentException("File header too short to verify signature");
            }

            if (".pdf".equals(extension)) {
                // PDF magic bytes: %PDF (0x25, 0x50, 0x44, 0x46)
                if (header[0] != 0x25 || header[1] != 0x50 || header[2] != 0x44 || header[3] != 0x46) {
                    throw new IllegalArgumentException("Invalid PDF file signature");
                }
            } else if (".png".equals(extension)) {
                // PNG magic bytes: 0x89 0x50 0x4E 0x47
                if (header[0] != (byte) 0x89 || header[1] != 0x50 || header[2] != 0x4E || header[3] != 0x47) {
                    throw new IllegalArgumentException("Invalid PNG file signature");
                }
            } else if (".jpg".equals(extension) || ".jpeg".equals(extension)) {
                // JPEG magic bytes: 0xFF 0xD8 0xFF
                if (header[0] != (byte) 0xFF || header[1] != (byte) 0xD8 || header[2] != (byte) 0xFF) {
                    throw new IllegalArgumentException("Invalid JPEG file signature");
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read file signature", e);
        }
    }

    private String computeSha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Error computing SHA-256 hash", e);
        }
    }

    public org.springframework.core.io.Resource loadFileAsResource(String filePath) {
        try {
            Path file = Paths.get(filePath).toAbsolutePath().normalize();
            if (!file.startsWith(this.storageDirectory)) {
                throw new SecurityException("Path traversal attempt detected!");
            }
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new org.aicte.sage.common.exception.ResourceNotFoundException("File not found or unreadable: " + filePath);
            }
        } catch (java.net.MalformedURLException e) {
            throw new org.aicte.sage.common.exception.ResourceNotFoundException("File not found: " + filePath, e);
        }
    }

    public Path getStorageDirectory() {
        return storageDirectory;
    }
}
