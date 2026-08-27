package com.campusconnect.controller;

import com.campusconnect.dto.AcademicResourceResponse;
import com.campusconnect.service.AcademicResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing student-facing Academic Resource endpoints.
 */
@RestController
@RequestMapping("/api/resources")
public class AcademicResourceController {

    private final AcademicResourceService resourceService;

    public AcademicResourceController(AcademicResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<List<AcademicResourceResponse>> getAllResources(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String search
    ) {
        List<AcademicResourceResponse> resources = resourceService.getAllResources(category, subject, resourceType, search);
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcademicResourceResponse> getResourceById(@PathVariable Long id) {
        AcademicResourceResponse resource = resourceService.getResourceById(id);
        return ResponseEntity.ok(resource);
    }
}
