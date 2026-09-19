package com.campusconnect.controller;

import com.campusconnect.dto.AcademicResourceResponse;
import com.campusconnect.entity.AcademicResource;
import com.campusconnect.entity.StudentProfile;
import com.campusconnect.entity.User;
import com.campusconnect.repository.StudentProfileRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.AcademicResourceService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing student-facing Academic Resource endpoints.
 */
@RestController
@RequestMapping("/api/resources")
public class AcademicResourceController {

    private final AcademicResourceService resourceService;
    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;

    public AcademicResourceController(
            AcademicResourceService resourceService,
            UserRepository userRepository,
            StudentProfileRepository profileRepository
    ) {
        this.resourceService = resourceService;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @GetMapping
    public ResponseEntity<List<AcademicResourceResponse>> getAllResources(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        String targetDept = null;
        String targetCourse = null;
        Integer targetYear = null;
        Integer targetSem = null;

        if (authentication != null && authentication.isAuthenticated()) {
            User u = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (u != null) {
                StudentProfile sp = profileRepository.findByUserId(u.getId()).orElse(null);
                if (sp != null) {
                    targetDept = sp.getDepartment();
                    targetCourse = sp.getCourse();
                    try { targetYear = Integer.parseInt(sp.getYear()); } catch (Exception ignored) {}
                    try { targetSem = Integer.parseInt(sp.getSemester()); } catch (Exception ignored) {}
                }
            }
        }

        List<AcademicResourceResponse> resources = resourceService.getAllResourcesForStudent(
                category, subject, resourceType, targetDept, targetCourse, targetYear, targetSem, search);
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcademicResourceResponse> getResourceById(@PathVariable Long id) {
        AcademicResourceResponse resource = resourceService.getResourceById(id);
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> viewResourceFile(@PathVariable Long id, Authentication authentication) {
        AcademicResource resourceEntity = resourceService.getResourceEntity(id);
        verifyAccessEligibility(resourceEntity, authentication);

        Resource fileResource = resourceService.getResourceFile(id);
        String contentType = resourceEntity.getFileContentType() != null
                ? resourceEntity.getFileContentType()
                : "application/octet-stream";

        String originalName = resourceEntity.getOriginalFileName() != null
                ? resourceEntity.getOriginalFileName()
                : "resource-file";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + originalName + "\"")
                .body(fileResource);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadResourceFile(@PathVariable Long id, Authentication authentication) {
        AcademicResource resourceEntity = resourceService.getResourceEntity(id);
        verifyAccessEligibility(resourceEntity, authentication);

        Resource fileResource = resourceService.getResourceFile(id);
        String contentType = resourceEntity.getFileContentType() != null
                ? resourceEntity.getFileContentType()
                : "application/octet-stream";

        String originalName = resourceEntity.getOriginalFileName() != null
                ? resourceEntity.getOriginalFileName()
                : "resource-file";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalName + "\"")
                .body(fileResource);
    }

    private void verifyAccessEligibility(AcademicResource resource, Authentication authentication) {
        if (resource == null) {
            throw new org.springframework.security.access.AccessDeniedException("Resource not found.");
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required.");
        }

        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            throw new org.springframework.security.access.AccessDeniedException("User not found.");
        }

        boolean isFacultyOwner = resource.getCreatedBy() != null && resource.getCreatedBy().getId().equals(user.getId());
        boolean isAdmin = user.getRoles().stream().anyMatch(r ->
                "ADMIN".equalsIgnoreCase(r.getName()) ||
                "SUPER_ADMIN".equalsIgnoreCase(r.getName()) ||
                "CLUB_ADMIN".equalsIgnoreCase(r.getName())
        );

        if (isFacultyOwner || isAdmin) {
            return;
        }

        if (!Boolean.TRUE.equals(resource.getPublished()) || !Boolean.TRUE.equals(resource.getActive())) {
            throw new org.springframework.security.access.AccessDeniedException("Resource is not published.");
        }

        StudentProfile sp = profileRepository.findByUserId(user.getId()).orElse(null);

        if (resource.getTargetDepartment() != null && !resource.getTargetDepartment().trim().isEmpty()) {
            if (sp == null || sp.getDepartment() == null || !sp.getDepartment().equalsIgnoreCase(resource.getTargetDepartment())) {
                throw new org.springframework.security.access.AccessDeniedException("Resource not targeted for student department.");
            }
        }

        if (resource.getTargetCourse() != null && !resource.getTargetCourse().trim().isEmpty()) {
            if (sp == null || sp.getCourse() == null || !sp.getCourse().equalsIgnoreCase(resource.getTargetCourse())) {
                throw new org.springframework.security.access.AccessDeniedException("Resource not targeted for student course.");
            }
        }

        if (resource.getTargetYear() != null && resource.getTargetYear() > 0) {
            int studentYear = 0;
            if (sp != null && sp.getYear() != null) {
                try { studentYear = Integer.parseInt(sp.getYear()); } catch (Exception ignored) {}
            }
            if (studentYear != resource.getTargetYear()) {
                throw new org.springframework.security.access.AccessDeniedException("Resource not targeted for student year.");
            }
        }

        if (resource.getTargetSemester() != null && resource.getTargetSemester() > 0) {
            int studentSem = 0;
            if (sp != null && sp.getSemester() != null) {
                try { studentSem = Integer.parseInt(sp.getSemester()); } catch (Exception ignored) {}
            }
            if (studentSem != resource.getTargetSemester()) {
                throw new org.springframework.security.access.AccessDeniedException("Resource not targeted for student semester.");
            }
        }
    }
}
