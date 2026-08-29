package com.campusconnect.controller;

import com.campusconnect.dto.AnnouncementResponse;
import com.campusconnect.entity.StudentProfile;
import com.campusconnect.entity.User;
import com.campusconnect.repository.StudentProfileRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.AnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing student-facing Campus Announcement endpoints.
 */
@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;

    public AnnouncementController(
            AnnouncementService announcementService,
            UserRepository userRepository,
            StudentProfileRepository profileRepository
    ) {
        this.announcementService = announcementService;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> getAllAnnouncements(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        String targetDept = null;
        String targetCourse = null;
        Integer targetYear = null;
        Integer targetSem = null;

        if (authentication != null && authentication.isAuthenticated()) {
            User u = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (u != null) {
                StudentProfile sp = profileRepository.findByUserId(u.getId()).orElse(null);
                if (sp != null) {
                    targetDept = sp.getDepartment();
                    targetCourse = sp.getCourse();
                    try { targetYear = Integer.parseInt(sp.getYear()); } catch (Exception ignored) {}
                    try { targetSem = Integer.parseInt(sp.getSemester()); } catch (Exception ignored) {}
                }
            }
        }

        List<AnnouncementResponse> announcements = announcementService.getAllAnnouncementsForStudent(
                category, targetDept, targetCourse, targetYear, targetSem, search);
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> getAnnouncementById(@PathVariable Long id) {
        AnnouncementResponse announcement = announcementService.getAnnouncementById(id);
        return ResponseEntity.ok(announcement);
    }
}
