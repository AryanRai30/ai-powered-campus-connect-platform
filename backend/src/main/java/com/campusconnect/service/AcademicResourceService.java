package com.campusconnect.service;

import com.campusconnect.dto.AcademicResourceResponse;
import com.campusconnect.entity.AcademicResource;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.AcademicResourceRepository;
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

    public AcademicResourceService(AcademicResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
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

    private AcademicResourceResponse mapToResponse(AcademicResource resource) {
        return AcademicResourceResponse.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .category(resource.getCategory())
                .subject(resource.getSubject())
                .resourceType(resource.getResourceType())
                .resourceUrl(resource.getResourceUrl())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }
}
