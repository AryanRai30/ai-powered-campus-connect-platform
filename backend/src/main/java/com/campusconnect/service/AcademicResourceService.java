package com.campusconnect.service;

import com.campusconnect.dto.AcademicResourceResponse;
import com.campusconnect.entity.AcademicResource;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.AcademicResourceRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service providing business logic for Campus Academic Resources & Study Materials.
 */
@Service
@Transactional
public class AcademicResourceService {

    private final AcademicResourceRepository resourceRepository;
    private final FileStorageService fileStorageService;

    public AcademicResourceService(
            AcademicResourceRepository resourceRepository,
            FileStorageService fileStorageService
    ) {
        this.resourceRepository = resourceRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public List<AcademicResourceResponse> getAllResources(
            String category,
            String subject,
            String resourceType,
            String search
    ) {
        List<AcademicResource> resources = resourceRepository.filterResources(category, subject, resourceType, search);

        return resources.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AcademicResourceResponse> getAllResourcesForStudent(
            String category,
            String subject,
            String resourceType,
            String targetDept,
            String targetCourse,
            Integer targetYear,
            Integer targetSem,
            String search
    ) {
        List<AcademicResource> resources = resourceRepository.filterResourcesForStudent(
                category, subject, resourceType, targetDept, targetCourse, targetYear, targetSem, search);

        return resources.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AcademicResourceResponse getResourceById(Long id) {
        AcademicResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic resource not found with id: " + id));
        return mapToResponse(resource);
    }

    @Transactional(readOnly = true)
    public Resource getResourceFile(Long id) {
        AcademicResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic resource not found with id: " + id));

        if (!Boolean.TRUE.equals(resource.getPublished()) || !Boolean.TRUE.equals(resource.getActive())) {
            throw new ResourceNotFoundException("Resource is not published or active.");
        }

        if (resource.getStoredFileName() == null || resource.getStoredFileName().trim().isEmpty()) {
            throw new ResourceNotFoundException("No file attached to resource id: " + id);
        }

        return fileStorageService.loadFileAsResource(resource.getStoredFileName());
    }

    @Transactional(readOnly = true)
    public AcademicResource getResourceEntity(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic resource not found with id: " + id));
    }

    private AcademicResourceResponse mapToResponse(AcademicResource resource) {
        boolean hasFile = resource.getStoredFileName() != null && !resource.getStoredFileName().trim().isEmpty();

        return AcademicResourceResponse.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .category(resource.getCategory())
                .subject(resource.getSubject())
                .resourceType(resource.getResourceType())
                .resourceUrl(resource.getResourceUrl())
                .originalFileName(resource.getOriginalFileName())
                .storedFileName(resource.getStoredFileName())
                .fileContentType(resource.getFileContentType())
                .fileSize(resource.getFileSize())
                .hasFile(hasFile)
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }
}

