package com.campusconnect.controller;

import com.campusconnect.dto.FacultyResourceRequest;
import com.campusconnect.dto.FacultyResourceResponse;
import com.campusconnect.service.FacultyResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty/resources")
public class FacultyResourceController {

    private final FacultyResourceService resourceService;

    public FacultyResourceController(FacultyResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    public ResponseEntity<FacultyResourceResponse> createResource(
            @Valid @RequestBody FacultyResourceRequest request,
            Authentication authentication
    ) {
        FacultyResourceResponse response = resourceService.createResource(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FacultyResourceResponse>> getFacultyResources(Authentication authentication) {
        List<FacultyResourceResponse> response = resourceService.getFacultyResources(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyResourceResponse> getFacultyResourceById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        FacultyResourceResponse response = resourceService.getFacultyResourceById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultyResourceResponse> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody FacultyResourceRequest request,
            Authentication authentication
    ) {
        FacultyResourceResponse response = resourceService.updateResource(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long id,
            Authentication authentication
    ) {
        resourceService.deleteResource(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<FacultyResourceResponse> publishResource(
            @PathVariable Long id,
            Authentication authentication
    ) {
        FacultyResourceResponse response = resourceService.publishResource(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<FacultyResourceResponse> unpublishResource(
            @PathVariable Long id,
            Authentication authentication
    ) {
        FacultyResourceResponse response = resourceService.unpublishResource(id, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
