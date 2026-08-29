package com.campusconnect.controller;

import com.campusconnect.dto.AcademicResourceResponse;
import com.campusconnect.entity.StudentProfile;
import com.campusconnect.entity.User;
import com.campusconnect.repository.StudentProfileRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.AcademicResourceService;
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
}
