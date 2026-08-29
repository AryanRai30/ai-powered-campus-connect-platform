package com.campusconnect.controller;

import com.campusconnect.dto.FacultyOpportunityApplicationResponse;
import com.campusconnect.dto.FacultyOpportunityRequest;
import com.campusconnect.dto.FacultyOpportunityResponse;
import com.campusconnect.service.FacultyOpportunityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty/opportunities")
public class FacultyOpportunityController {

    private final FacultyOpportunityService opportunityService;

    public FacultyOpportunityController(FacultyOpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @PostMapping
    public ResponseEntity<FacultyOpportunityResponse> createOpportunity(
            @Valid @RequestBody FacultyOpportunityRequest request,
            Authentication authentication
    ) {
        FacultyOpportunityResponse response = opportunityService.createOpportunity(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FacultyOpportunityResponse>> getFacultyOpportunities(Authentication authentication) {
        List<FacultyOpportunityResponse> response = opportunityService.getFacultyOpportunities(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyOpportunityResponse> getFacultyOpportunityById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        FacultyOpportunityResponse response = opportunityService.getFacultyOpportunityById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultyOpportunityResponse> updateOpportunity(
            @PathVariable Long id,
            @Valid @RequestBody FacultyOpportunityRequest request,
            Authentication authentication
    ) {
        FacultyOpportunityResponse response = opportunityService.updateOpportunity(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOpportunity(
            @PathVariable Long id,
            Authentication authentication
    ) {
        opportunityService.deleteOpportunity(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<FacultyOpportunityResponse> publishOpportunity(
            @PathVariable Long id,
            Authentication authentication
    ) {
        FacultyOpportunityResponse response = opportunityService.publishOpportunity(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<FacultyOpportunityResponse> unpublishOpportunity(
            @PathVariable Long id,
            Authentication authentication
    ) {
        FacultyOpportunityResponse response = opportunityService.unpublishOpportunity(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/applications")
    public ResponseEntity<List<FacultyOpportunityApplicationResponse>> getOpportunityApplications(
            @PathVariable Long id,
            Authentication authentication
    ) {
        List<FacultyOpportunityApplicationResponse> response = opportunityService.getOpportunityApplications(id, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
