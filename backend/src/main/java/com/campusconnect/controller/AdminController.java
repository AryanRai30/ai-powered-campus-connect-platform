package com.campusconnect.controller;

import com.campusconnect.dto.AdminStatsResponse;
import com.campusconnect.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing Administration endpoints.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getAdminStats(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        AdminStatsResponse response = adminService.getAdminStats(currentUserEmail);
        return ResponseEntity.ok(response);
    }
}
