package com.campusconnect.controller;

import com.campusconnect.dto.AdminFacultyDetailResponse;
import com.campusconnect.dto.AdminFacultyResponse;
import com.campusconnect.dto.CreateFacultyRequest;
import com.campusconnect.dto.UpdateFacultyRequest;
import com.campusconnect.dto.UpdateFacultyStatusRequest;
import com.campusconnect.service.AdminFacultyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller exposing Administrator Faculty Management endpoints.
 */
@RestController
@RequestMapping("/api/admin/faculty")
public class AdminFacultyController {

    private final AdminFacultyService adminFacultyService;

    public AdminFacultyController(AdminFacultyService adminFacultyService) {
        this.adminFacultyService = adminFacultyService;
    }

    @GetMapping
    public ResponseEntity<List<AdminFacultyResponse>> getFacultyList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        List<AdminFacultyResponse> facultyList = adminFacultyService.getFacultyList(adminEmail, status, search);
        return ResponseEntity.ok(facultyList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminFacultyDetailResponse> getFacultyById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        AdminFacultyDetailResponse faculty = adminFacultyService.getFacultyById(adminEmail, id);
        return ResponseEntity.ok(faculty);
    }

    @PostMapping
    public ResponseEntity<AdminFacultyResponse> createFaculty(
            @Valid @RequestBody CreateFacultyRequest request,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        AdminFacultyResponse createdFaculty = adminFacultyService.createFaculty(adminEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFaculty);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminFacultyResponse> updateFaculty(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFacultyRequest request,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        AdminFacultyResponse updatedFaculty = adminFacultyService.updateFaculty(adminEmail, id, request);
        return ResponseEntity.ok(updatedFaculty);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminFacultyResponse> updateFacultyStatus(
            @PathVariable Long id,
            @RequestBody UpdateFacultyStatusRequest request,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        AdminFacultyResponse updatedFaculty = adminFacultyService.updateFacultyStatus(adminEmail, id, request.isActive());
        return ResponseEntity.ok(updatedFaculty);
    }
}
