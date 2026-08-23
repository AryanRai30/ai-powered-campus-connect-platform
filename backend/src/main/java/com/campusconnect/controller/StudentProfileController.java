package com.campusconnect.controller;

import com.campusconnect.dto.StudentProfileRequest;
import com.campusconnect.dto.StudentProfileResponse;
import com.campusconnect.service.StudentProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller exposing Student Profile management endpoints.
 */
@RestController
@RequestMapping("/api/student/profile")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    public StudentProfileController(StudentProfileService studentProfileService) {
        this.studentProfileService = studentProfileService;
    }

    @PostMapping
    public ResponseEntity<StudentProfileResponse> createProfile(
            @Valid @RequestBody StudentProfileRequest request,
            Authentication authentication
    ) {
        String currentUserEmail = authentication.getName();
        StudentProfileResponse response = studentProfileService.createProfile(request, currentUserEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<StudentProfileResponse> getCurrentUserProfile(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        StudentProfileResponse response = studentProfileService.getCurrentUserProfile(currentUserEmail);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<StudentProfileResponse> updateCurrentUserProfile(
            @Valid @RequestBody StudentProfileRequest request,
            Authentication authentication
    ) {
        String currentUserEmail = authentication.getName();
        StudentProfileResponse response = studentProfileService.updateCurrentUserProfile(request, currentUserEmail);
        return ResponseEntity.ok(response);
    }
}
