package com.campusconnect.controller;

import com.campusconnect.ai.rag.dto.IngestionSummaryDto;
import com.campusconnect.ai.rag.service.CampusContentIngestionService;
import com.campusconnect.dto.AdminStatsResponse;
import com.campusconnect.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing Administration endpoints.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final CampusContentIngestionService campusContentIngestionService;

    public AdminController(AdminService adminService, CampusContentIngestionService campusContentIngestionService) {
        this.adminService = adminService;
        this.campusContentIngestionService = campusContentIngestionService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getAdminStats(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        AdminStatsResponse response = adminService.getAdminStats(currentUserEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rag/sync")
    public ResponseEntity<IngestionSummaryDto> syncRagKnowledgeBase() {
        IngestionSummaryDto summary = campusContentIngestionService.syncAllCampusContent();
        return ResponseEntity.ok(summary);
    }
}
