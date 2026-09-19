package com.campusconnect.controller;

import com.campusconnect.dto.EventResponse;
import com.campusconnect.dto.RegistrationStatusResponse;
import com.campusconnect.entity.StudentProfile;
import com.campusconnect.entity.User;
import com.campusconnect.repository.StudentProfileRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing student-facing Campus Event endpoints.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;

    public EventController(
            EventService eventService,
            UserRepository userRepository,
            StudentProfileRepository profileRepository
    ) {
        this.eventService = eventService;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        String targetDept = null;
        String targetCourse = null;
        Integer targetYear = null;
        Integer targetSem = null;

        if (userEmail != null) {
            User u = userRepository.findByEmail(userEmail).orElse(null);
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

        List<EventResponse> events = eventService.getAllEventsForStudent(
                category, targetDept, targetCourse, targetYear, targetSem, search, userEmail);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        EventResponse event = eventService.getEventById(id, userEmail);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/my-events")
    public ResponseEntity<List<EventResponse>> getMyEvents(Authentication authentication) {
        String userEmail = authentication.getName();
        List<EventResponse> myEvents = eventService.getMyEvents(userEmail);
        return ResponseEntity.ok(myEvents);
    }

    @PostMapping("/{eventId}/register")
    public ResponseEntity<RegistrationStatusResponse> registerForEvent(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        RegistrationStatusResponse response = eventService.registerForEvent(eventId, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{eventId}/registration")
    public ResponseEntity<RegistrationStatusResponse> getRegistrationStatus(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        RegistrationStatusResponse response = eventService.getRegistrationStatus(eventId, userEmail);
        return ResponseEntity.ok(response);
    }
}
