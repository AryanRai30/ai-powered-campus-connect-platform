package com.campusconnect.controller;

import com.campusconnect.dto.AdminPlatformAnalyticsResponse;
import com.campusconnect.dto.FacultyActivityAnalyticsResponse;
import com.campusconnect.dto.StudentActivityAnalyticsResponse;
import com.campusconnect.service.ActivityAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing Phase 12.1 Activity Analytics endpoints.
 */
@RestController
@RequestMapping("/api/analytics")
public class ActivityAnalyticsController {

    private final ActivityAnalyticsService analyticsService;

    public ActivityAnalyticsController(ActivityAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/student")
    public ResponseEntity<StudentActivityAnalyticsResponse> getStudentActivityAnalytics(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        StudentActivityAnalyticsResponse response = analyticsService.getStudentActivityAnalytics(currentUserEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/faculty")
    public ResponseEntity<FacultyActivityAnalyticsResponse> getFacultyActivityAnalytics(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        FacultyActivityAnalyticsResponse response = analyticsService.getFacultyActivityAnalytics(currentUserEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<AdminPlatformAnalyticsResponse> getAdminPlatformAnalytics(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        AdminPlatformAnalyticsResponse response = analyticsService.getAdminPlatformAnalytics(currentUserEmail);
        return ResponseEntity.ok(response);
    }
}
