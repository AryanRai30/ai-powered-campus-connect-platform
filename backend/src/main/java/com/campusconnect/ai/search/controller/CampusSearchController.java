package com.campusconnect.ai.search.controller;

import com.campusconnect.ai.search.dto.CampusSearchRequest;
import com.campusconnect.ai.search.dto.CampusSearchResponse;
import com.campusconnect.ai.search.service.CampusSearchService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing REST endpoints for Phase 11.4 Semantic Campus Search.
 * Endpoint: POST /api/ai/search
 */
@RestController
@RequestMapping("/api/ai")
public class CampusSearchController {

    private static final Logger log = LoggerFactory.getLogger(CampusSearchController.class);

    private final CampusSearchService campusSearchService;

    @Autowired
    public CampusSearchController(CampusSearchService campusSearchService) {
        this.campusSearchService = campusSearchService;
    }

    /**
     * POST /api/ai/search
     * Queries Qdrant campus knowledge base and returns ranked search results with authorization filtering.
     */
    @PostMapping("/search")
    public ResponseEntity<CampusSearchResponse> searchCampus(@Valid @RequestBody CampusSearchRequest request) {
        String email = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            email = authentication.getName();
        }

        log.info("Received semantic campus search request from user '{}': {}", email != null ? email : "anonymous", request.getQuery());
        CampusSearchResponse response = campusSearchService.searchCampusKnowledge(request, email);
        return ResponseEntity.ok(response);
    }
}
