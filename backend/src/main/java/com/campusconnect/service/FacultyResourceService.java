package com.campusconnect.service;

import com.campusconnect.dto.FacultyResourceRequest;
import com.campusconnect.dto.FacultyResourceResponse;
import com.campusconnect.entity.AcademicResource;
import com.campusconnect.entity.User;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.AcademicResourceRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacultyResourceService {

    private final AcademicResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public FacultyResourceService(
            AcademicResourceRepository resourceRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService
    ) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    private User getAuthenticatedFaculty(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        boolean isFaculty = user.getRoles().stream()
                .anyMatch(r -> "FACULTY".equalsIgnoreCase(r.getName()));

        if (!isFaculty) {
            throw new AccessDeniedException("User does not possess FACULTY role.");
        }
        return user;
    }

    private AcademicResource getResourceAndVerifyOwnership(Long id, User faculty) {
        AcademicResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic resource not found with id: " + id));

        if (resource.getCreatedBy() == null || !resource.getCreatedBy().getId().equals(faculty.getId())) {
            throw new AccessDeniedException("Access Denied: You do not have permission to modify this resource.");
        }
        return resource;
    }

    public FacultyResourceResponse createResource(FacultyResourceRequest request, String facultyEmail) {
        return createResource(request, null, facultyEmail);
    }

    public FacultyResourceResponse createResource(FacultyResourceRequest request, MultipartFile file, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);

        FileStorageService.FileUploadResult fileResult = null;
        if (file != null && !file.isEmpty()) {
            fileResult = fileStorageService.storeFile(file);
        }

        String resourceType = request.getResourceType();
        if ((resourceType == null || resourceType.trim().isEmpty()) && fileResult != null) {
            resourceType = deriveResourceTypeFromContentType(fileResult.getFileContentType(), fileResult.getOriginalFileName());
        }

        AcademicResource resource = AcademicResource.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .subject(request.getSubject())
                .resourceType(resourceType != null ? resourceType : "NOTES")
                .resourceUrl(request.getResourceUrl())
                .originalFileName(fileResult != null ? fileResult.getOriginalFileName() : null)
                .storedFileName(fileResult != null ? fileResult.getStoredFileName() : null)
                .fileContentType(fileResult != null ? fileResult.getFileContentType() : null)
                .fileSize(fileResult != null ? fileResult.getFileSize() : null)
                .fileStoragePath(fileResult != null ? fileResult.getFileStoragePath() : null)
                .targetDepartment(request.getTargetDepartment())
                .targetCourse(request.getTargetCourse())
                .targetYear(request.getTargetYear())
                .targetSemester(request.getTargetSemester())
                .targetSection(request.getTargetSection())
                .published(request.getPublished() != null ? request.getPublished() : false)
                .active(true)
                .createdBy(faculty)
                .build();

        AcademicResource saved = resourceRepository.save(resource);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FacultyResourceResponse> getFacultyResources(String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        List<AcademicResource> resources = resourceRepository.findAllByCreatedByOrderByIdDesc(faculty);
        return resources.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FacultyResourceResponse getFacultyResourceById(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        AcademicResource resource = getResourceAndVerifyOwnership(id, faculty);
        return mapToResponse(resource);
    }

    public FacultyResourceResponse updateResource(Long id, FacultyResourceRequest request, String facultyEmail) {
        return updateResource(id, request, null, facultyEmail);
    }

    public FacultyResourceResponse updateResource(Long id, FacultyResourceRequest request, MultipartFile file, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        AcademicResource resource = getResourceAndVerifyOwnership(id, faculty);

        if (file != null && !file.isEmpty()) {
            if (resource.getStoredFileName() != null) {
                fileStorageService.deleteFile(resource.getStoredFileName());
            }
            FileStorageService.FileUploadResult fileResult = fileStorageService.storeFile(file);
            resource.setOriginalFileName(fileResult.getOriginalFileName());
            resource.setStoredFileName(fileResult.getStoredFileName());
            resource.setFileContentType(fileResult.getFileContentType());
            resource.setFileSize(fileResult.getFileSize());
            resource.setFileStoragePath(fileResult.getFileStoragePath());

            if (request.getResourceType() == null || request.getResourceType().trim().isEmpty()) {
                resource.setResourceType(deriveResourceTypeFromContentType(fileResult.getFileContentType(), fileResult.getOriginalFileName()));
            }
        }

        resource.setTitle(request.getTitle());
        resource.setDescription(request.getDescription());
        resource.setCategory(request.getCategory());
        resource.setSubject(request.getSubject());
        if (request.getResourceType() != null && !request.getResourceType().trim().isEmpty()) {
            resource.setResourceType(request.getResourceType());
        }
        resource.setResourceUrl(request.getResourceUrl());
        resource.setTargetDepartment(request.getTargetDepartment());
        resource.setTargetCourse(request.getTargetCourse());
        resource.setTargetYear(request.getTargetYear());
        resource.setTargetSemester(request.getTargetSemester());
        resource.setTargetSection(request.getTargetSection());
        if (request.getPublished() != null) {
            resource.setPublished(request.getPublished());
        }

        AcademicResource updated = resourceRepository.save(resource);
        return mapToResponse(updated);
    }

    public void deleteResource(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        AcademicResource resource = getResourceAndVerifyOwnership(id, faculty);

        if (resource.getStoredFileName() != null) {
            fileStorageService.deleteFile(resource.getStoredFileName());
        }

        resourceRepository.delete(resource);
    }

    public FacultyResourceResponse publishResource(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        AcademicResource resource = getResourceAndVerifyOwnership(id, faculty);
        resource.setPublished(true);
        AcademicResource updated = resourceRepository.save(resource);
        return mapToResponse(updated);
    }

    public FacultyResourceResponse unpublishResource(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        AcademicResource resource = getResourceAndVerifyOwnership(id, faculty);
        resource.setPublished(false);
        AcademicResource updated = resourceRepository.save(resource);
        return mapToResponse(updated);
    }

    private String deriveResourceTypeFromContentType(String contentType, String filename) {
        if (contentType != null) {
            String lower = contentType.toLowerCase();
            if (lower.contains("pdf")) return "PDF";
            if (lower.contains("powerpoint") || lower.contains("presentation")) return "PPT";
            if (lower.contains("word") || lower.contains("document")) return "DOC";
            if (lower.contains("video")) return "VIDEO";
            if (lower.contains("image")) return "IMAGE";
            if (lower.contains("zip")) return "ZIP";
        }
        if (filename != null) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".pdf")) return "PDF";
            if (lower.endsWith(".ppt") || lower.endsWith(".pptx")) return "PPT";
            if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "DOC";
            if (lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.endsWith(".mov")) return "VIDEO";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) return "IMAGE";
            if (lower.endsWith(".zip")) return "ZIP";
        }
        return "NOTES";
    }

    private FacultyResourceResponse mapToResponse(AcademicResource r) {
        boolean hasFile = r.getStoredFileName() != null && !r.getStoredFileName().trim().isEmpty();

        return FacultyResourceResponse.builder()
                .id(r.getId())
                .title(r.getTitle())
                .description(r.getDescription())
                .category(r.getCategory())
                .subject(r.getSubject())
                .resourceType(r.getResourceType())
                .resourceUrl(r.getResourceUrl())
                .originalFileName(r.getOriginalFileName())
                .storedFileName(r.getStoredFileName())
                .fileContentType(r.getFileContentType())
                .fileSize(r.getFileSize())
                .hasFile(hasFile)
                .published(r.getPublished())
                .active(r.getActive())
                .targetDepartment(r.getTargetDepartment())
                .targetCourse(r.getTargetCourse())
                .targetYear(r.getTargetYear())
                .targetSemester(r.getTargetSemester())
                .targetSection(r.getTargetSection())
                .createdByEmail(r.getCreatedBy() != null ? r.getCreatedBy().getEmail() : null)
                .createdByName(r.getCreatedBy() != null ? r.getCreatedBy().getFirstName() + " " + r.getCreatedBy().getLastName() : null)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}

