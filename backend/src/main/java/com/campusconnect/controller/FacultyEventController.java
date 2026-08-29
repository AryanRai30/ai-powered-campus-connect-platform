package com.campusconnect.controller;

import com.campusconnect.dto.EventRegistrationResponse;
import com.campusconnect.dto.FacultyEventRequest;
import com.campusconnect.dto.FacultyEventResponse;
import com.campusconnect.service.FacultyEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty/events")
public class FacultyEventController {

    private final FacultyEventService eventService;

    public FacultyEventController(FacultyEventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<FacultyEventResponse> createEvent(
            @Valid @RequestBody FacultyEventRequest request,
            Authentication authentication
    ) {
        FacultyEventResponse response = eventService.createEvent(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FacultyEventResponse>> getFacultyEvents(Authentication authentication) {
        List<FacultyEventResponse> response = eventService.getFacultyEvents(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyEventResponse> getFacultyEventById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        FacultyEventResponse response = eventService.getFacultyEventById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultyEventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody FacultyEventRequest request,
            Authentication authentication
    ) {
        FacultyEventResponse response = eventService.updateEvent(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            Authentication authentication
    ) {
        eventService.deleteEvent(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<FacultyEventResponse> publishEvent(
            @PathVariable Long id,
            Authentication authentication
    ) {
        FacultyEventResponse response = eventService.publishEvent(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<FacultyEventResponse> unpublishEvent(
            @PathVariable Long id,
            Authentication authentication
    ) {
        FacultyEventResponse response = eventService.unpublishEvent(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/registrations")
    public ResponseEntity<List<EventRegistrationResponse>> getEventRegistrations(
            @PathVariable Long id,
            Authentication authentication
    ) {
        List<EventRegistrationResponse> response = eventService.getEventRegistrations(id, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
