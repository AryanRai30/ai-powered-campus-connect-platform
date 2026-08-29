package com.campusconnect.controller;

import com.campusconnect.dto.ClubMemberResponse;
import com.campusconnect.dto.FacultyClubRequest;
import com.campusconnect.dto.FacultyClubResponse;
import com.campusconnect.service.FacultyClubService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty/clubs")
public class FacultyClubController {

    private final FacultyClubService clubService;

    public FacultyClubController(FacultyClubService clubService) {
        this.clubService = clubService;
    }

    @PostMapping
    public ResponseEntity<FacultyClubResponse> createClub(
            @Valid @RequestBody FacultyClubRequest request,
            Authentication authentication
    ) {
        FacultyClubResponse response = clubService.createClub(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FacultyClubResponse>> getFacultyClubs(Authentication authentication) {
        List<FacultyClubResponse> response = clubService.getFacultyClubs(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyClubResponse> getFacultyClubById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        FacultyClubResponse response = clubService.getFacultyClubById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultyClubResponse> updateClub(
            @PathVariable Long id,
            @Valid @RequestBody FacultyClubRequest request,
            Authentication authentication
    ) {
        FacultyClubResponse response = clubService.updateClub(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClub(
            @PathVariable Long id,
            Authentication authentication
    ) {
        clubService.deleteClub(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<FacultyClubResponse> publishClub(
            @PathVariable Long id,
            Authentication authentication
    ) {
        FacultyClubResponse response = clubService.publishClub(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<FacultyClubResponse> unpublishClub(
            @PathVariable Long id,
            Authentication authentication
    ) {
        FacultyClubResponse response = clubService.unpublishClub(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<ClubMemberResponse>> getClubMembers(
            @PathVariable Long id,
            Authentication authentication
    ) {
        List<ClubMemberResponse> response = clubService.getClubMembers(id, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
