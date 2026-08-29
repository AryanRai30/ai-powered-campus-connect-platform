package com.campusconnect.controller;

import com.campusconnect.dto.FacultyDashboardResponse;
import com.campusconnect.dto.FacultyDashboardStatsResponse;
import com.campusconnect.dto.FacultyStudentResponse;
import com.campusconnect.service.FacultyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller exposing Faculty endpoints.
 */
@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<FacultyDashboardResponse> getFacultyDashboard(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        FacultyDashboardResponse response = facultyService.getFacultyDashboard(currentUserEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<FacultyDashboardStatsResponse> getFacultyStats(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        FacultyDashboardStatsResponse response = facultyService.getFacultyStats(currentUserEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/students")
    public ResponseEntity<List<FacultyStudentResponse>> getFacultyStudents(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        String currentUserEmail = authentication.getName();
        List<FacultyStudentResponse> response = facultyService.getFacultyStudents(
                currentUserEmail, department, course, year, semester, search);
        return ResponseEntity.ok(response);
    }
}
