package com.campusconnect.controller;

import com.campusconnect.dto.UserProfileResponse;
import com.campusconnect.dto.UserProfileUpdateRequest;
import com.campusconnect.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller exposing unified authenticated user self-profile endpoints for Student, Faculty, and Admin.
 */
@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        UserProfileResponse response = userProfileService.getUserProfile(currentUserEmail);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
            @Valid @RequestBody UserProfileUpdateRequest request,
            Authentication authentication
    ) {
        String currentUserEmail = authentication.getName();
        UserProfileResponse response = userProfileService.updateUserProfile(request, currentUserEmail);
        return ResponseEntity.ok(response);
    }
}
