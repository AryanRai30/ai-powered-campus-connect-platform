package com.campusconnect.controller;

import com.campusconnect.dto.ClubMembershipStatusResponse;
import com.campusconnect.dto.ClubResponse;
import com.campusconnect.service.ClubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing student-facing Campus Clubs & Communities endpoints.
 */
@RestController
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @GetMapping
    public ResponseEntity<List<ClubResponse>> getAllClubs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        List<ClubResponse> clubs = clubService.getAllClubs(category, search, userEmail);
        return ResponseEntity.ok(clubs);
    }

    @GetMapping("/my-clubs")
    public ResponseEntity<List<ClubResponse>> getMyClubs(Authentication authentication) {
        String userEmail = authentication.getName();
        List<ClubResponse> myClubs = clubService.getMyClubs(userEmail);
        return ResponseEntity.ok(myClubs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClubResponse> getClubById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        ClubResponse club = clubService.getClubById(id, userEmail);
        return ResponseEntity.ok(club);
    }

    @PostMapping("/{clubId}/join")
    public ResponseEntity<ClubMembershipStatusResponse> joinClub(
            @PathVariable Long clubId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ClubMembershipStatusResponse response = clubService.joinClub(clubId, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{clubId}/membership")
    public ResponseEntity<ClubMembershipStatusResponse> getMembershipStatus(
            @PathVariable Long clubId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ClubMembershipStatusResponse response = clubService.getMembershipStatus(clubId, userEmail);
        return ResponseEntity.ok(response);
    }
}
