package com.campusconnect.controller;

import com.campusconnect.dto.FacultyResourceRequest;
import com.campusconnect.dto.FacultyResourceResponse;
import com.campusconnect.service.FacultyResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/faculty/resources")
public class FacultyResourceController {

    private final FacultyResourceService resourceService;

    public FacultyResourceController(FacultyResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FacultyResourceResponse> createResourceMultipart(
            @Valid @ModelAttribute FacultyResourceRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication authentication
    ) {
        FacultyResourceResponse response = resourceService.createResource(request, file, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FacultyResourceResponse> createResourceJson(
            @Valid @RequestBody FacultyResourceRequest request,
            Authentication authentication
    ) {
        FacultyResourceResponse response = resourceService.createResource(request, null, authentication.getName());
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

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FacultyResourceResponse> updateResourceMultipart(
            @PathVariable Long id,
            @Valid @ModelAttribute FacultyResourceRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication authentication
    ) {
        FacultyResourceResponse response = resourceService.updateResource(id, request, file, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FacultyResourceResponse> updateResourceJson(
            @PathVariable Long id,
            @Valid @RequestBody FacultyResourceRequest request,
            Authentication authentication
    ) {
        FacultyResourceResponse response = resourceService.updateResource(id, request, null, authentication.getName());
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
