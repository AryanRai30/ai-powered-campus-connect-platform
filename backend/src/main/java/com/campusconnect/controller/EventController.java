package com.campusconnect.controller;

import com.campusconnect.dto.EventResponse;
import com.campusconnect.dto.RegistrationStatusResponse;
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

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        List<EventResponse> events = eventService.getAllEvents(category, search, userEmail);
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
