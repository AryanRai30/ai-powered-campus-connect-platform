package com.campusconnect.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "pdf", "ppt", "pptx", "doc", "docx", "txt",
            "jpg", "jpeg", "png", "webp",
            "mp4", "webm", "mov", "zip"
    ));

    private static final Set<String> DISALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "exe", "bat", "cmd", "sh", "ps1", "jar", "php", "jsp", "asp", "aspx",
            "py", "js", "vbs", "wsf", "com", "scr", "cpl", "msi", "htm", "html"
    ));

    @Value("${app.upload.dir:uploads/resources}")
    private String uploadDir;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the upload directory: " + this.fileStorageLocation, ex);
        }
    }

    public FileUploadResult storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store an empty file.");
        }

        String rawFilename = file.getOriginalFilename();
        if (rawFilename == null || rawFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File must have a valid original filename.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(rawFilename));

        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new IllegalArgumentException("Filename contains invalid path sequence: " + originalFilename);
        }

        String extension = getFileExtension(originalFilename).toLowerCase();

        if (DISALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Executable or dangerous file types are strictly prohibited: ." + extension);
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported file type: ." + extension + ". Allowed types: PDF, PPT, DOC, TXT, Images (JPG, PNG, WEBP), Videos (MP4, WEBM, MOV), ZIP.");
        }

        String storedFileName = UUID.randomUUID().toString() + "_" + originalFilename;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String contentType = file.getContentType();
            if (contentType == null || contentType.trim().isEmpty()) {
                contentType = resolveContentTypeFromExtension(extension);
            }

            return new FileUploadResult(
                    originalFilename,
                    storedFileName,
                    contentType,
                    file.getSize(),
                    targetLocation.toString()
            );
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String storedFileName) {
        try {
            if (storedFileName == null || storedFileName.trim().isEmpty()) {
                throw new FileNotFoundException("Stored filename is missing.");
            }

            Path filePath = this.fileStorageLocation.resolve(storedFileName).normalize();
            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new SecurityException("Cannot access file outside storage directory.");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found or not readable: " + storedFileName);
            }
        } catch (MalformedURLException | FileNotFoundException ex) {
            throw new RuntimeException("File not found: " + storedFileName, ex);
        }
    }

    public void deleteFile(String storedFileName) {
        if (storedFileName == null || storedFileName.trim().isEmpty()) {
            return;
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(storedFileName).normalize();
            if (filePath.startsWith(this.fileStorageLocation)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ex) {
            System.err.println("Failed to delete stored file: " + storedFileName + ". Reason: " + ex.getMessage());
        }
    }

    public String resolveContentTypeFromExtension(String extension) {
        switch (extension.toLowerCase()) {
            case "pdf": return "application/pdf";
            case "ppt": return "application/vnd.ms-powerpoint";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "txt": return "text/plain";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "webp": return "image/webp";
            case "mp4": return "video/mp4";
            case "webm": return "video/webm";
            case "mov": return "video/quicktime";
            case "zip": return "application/zip";
            default: return "application/octet-stream";
        }
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastIndex + 1);
    }

    public static class FileUploadResult {
        private final String originalFileName;
        private final String storedFileName;
        private final String fileContentType;
        private final Long fileSize;
        private final String fileStoragePath;

        public FileUploadResult(String originalFileName, String storedFileName, String fileContentType, Long fileSize, String fileStoragePath) {
            this.originalFileName = originalFileName;
            this.storedFileName = storedFileName;
            this.fileContentType = fileContentType;
            this.fileSize = fileSize;
            this.fileStoragePath = fileStoragePath;
        }

        public String getOriginalFileName() { return originalFileName; }
        public String getStoredFileName() { return storedFileName; }
        public String getFileContentType() { return fileContentType; }
        public Long getFileSize() { return fileSize; }
        public String getFileStoragePath() { return fileStoragePath; }
    }
}
