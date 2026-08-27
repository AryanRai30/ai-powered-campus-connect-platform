package com.campusconnect.controller;

import com.campusconnect.dto.OpportunityApplicationStatusResponse;
import com.campusconnect.dto.OpportunityBookmarkStatusResponse;
import com.campusconnect.dto.OpportunityResponse;
import com.campusconnect.service.OpportunityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing student-facing Opportunities & Career Support endpoints.
 */
@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping
    public ResponseEntity<List<OpportunityResponse>> getAllOpportunities(
            @RequestParam(required = false) String opportunityType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        List<OpportunityResponse> opportunities = opportunityService.getAllOpportunities(opportunityType, location, search, userEmail);
        return ResponseEntity.ok(opportunities);
    }

    @GetMapping("/my-bookmarks")
    public ResponseEntity<List<OpportunityResponse>> getMyBookmarks(Authentication authentication) {
        String userEmail = authentication.getName();
        List<OpportunityResponse> bookmarks = opportunityService.getMyBookmarks(userEmail);
        return ResponseEntity.ok(bookmarks);
    }

    @GetMapping("/my-applications")
    public ResponseEntity<List<OpportunityResponse>> getMyApplications(Authentication authentication) {
        String userEmail = authentication.getName();
        List<OpportunityResponse> applications = opportunityService.getMyApplications(userEmail);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OpportunityResponse> getOpportunityById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        OpportunityResponse opportunity = opportunityService.getOpportunityById(id, userEmail);
        return ResponseEntity.ok(opportunity);
    }

    @PostMapping("/{opportunityId}/bookmark")
    public ResponseEntity<OpportunityBookmarkStatusResponse> bookmarkOpportunity(
            @PathVariable Long opportunityId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        OpportunityBookmarkStatusResponse response = opportunityService.bookmarkOpportunity(opportunityId, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{opportunityId}/bookmark")
    public ResponseEntity<OpportunityBookmarkStatusResponse> getBookmarkStatus(
            @PathVariable Long opportunityId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        OpportunityBookmarkStatusResponse response = opportunityService.getBookmarkStatus(opportunityId, userEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{opportunityId}/apply")
    public ResponseEntity<OpportunityApplicationStatusResponse> applyForOpportunity(
            @PathVariable Long opportunityId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        OpportunityApplicationStatusResponse response = opportunityService.applyForOpportunity(opportunityId, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{opportunityId}/application")
    public ResponseEntity<OpportunityApplicationStatusResponse> getApplicationStatus(
            @PathVariable Long opportunityId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        OpportunityApplicationStatusResponse response = opportunityService.getApplicationStatus(opportunityId, userEmail);
        return ResponseEntity.ok(response);
    }
}
