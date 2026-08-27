package com.campusconnect.controller;

import com.campusconnect.dto.AnnouncementResponse;
import com.campusconnect.service.AnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing student-facing Campus Announcement endpoints.
 */
@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> getAllAnnouncements(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        List<AnnouncementResponse> announcements = announcementService.getAllAnnouncements(category, search);
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> getAnnouncementById(@PathVariable Long id) {
        AnnouncementResponse announcement = announcementService.getAnnouncementById(id);
        return ResponseEntity.ok(announcement);
    }
}
