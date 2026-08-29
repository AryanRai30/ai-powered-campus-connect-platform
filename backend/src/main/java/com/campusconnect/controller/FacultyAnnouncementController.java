package com.campusconnect.controller;

import com.campusconnect.dto.FacultyAnnouncementRequest;
import com.campusconnect.dto.FacultyAnnouncementResponse;
import com.campusconnect.service.FacultyAnnouncementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty/announcements")
public class FacultyAnnouncementController {

    private final FacultyAnnouncementService announcementService;

    public FacultyAnnouncementController(FacultyAnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @PostMapping
    public ResponseEntity<FacultyAnnouncementResponse> createAnnouncement(
            @Valid @RequestBody FacultyAnnouncementRequest request,
            Authentication authentication
    ) {
        FacultyAnnouncementResponse response = announcementService.createAnnouncement(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FacultyAnnouncementResponse>> getFacultyAnnouncements(Authentication authentication) {
        List<FacultyAnnouncementResponse> response = announcementService.getFacultyAnnouncements(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyAnnouncementResponse> getFacultyAnnouncementById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        FacultyAnnouncementResponse response = announcementService.getFacultyAnnouncementById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultyAnnouncementResponse> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody FacultyAnnouncementRequest request,
            Authentication authentication
    ) {
        FacultyAnnouncementResponse response = announcementService.updateAnnouncement(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable Long id,
            Authentication authentication
    ) {
        announcementService.deleteAnnouncement(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<FacultyAnnouncementResponse> publishAnnouncement(
            @PathVariable Long id,
            Authentication authentication
    ) {
        FacultyAnnouncementResponse response = announcementService.publishAnnouncement(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<FacultyAnnouncementResponse> unpublishAnnouncement(
            @PathVariable Long id,
            Authentication authentication
    ) {
        FacultyAnnouncementResponse response = announcementService.unpublishAnnouncement(id, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
