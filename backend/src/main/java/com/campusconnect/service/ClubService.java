package com.campusconnect.service;

import com.campusconnect.dto.ClubMembershipStatusResponse;
import com.campusconnect.dto.ClubResponse;
import com.campusconnect.entity.Club;
import com.campusconnect.entity.ClubMembership;
import com.campusconnect.entity.User;
import com.campusconnect.exception.ClubAlreadyJoinedException;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.ClubMembershipRepository;
import com.campusconnect.repository.ClubRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service providing business logic for Campus Clubs & Communities and Memberships.
 */
@Service
@Transactional
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public ClubService(
            ClubRepository clubRepository,
            ClubMembershipRepository membershipRepository,
            UserRepository userRepository
    ) {
        this.clubRepository = clubRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ClubResponse> getAllClubs(String category, String search, String currentUserEmail) {
        User currentUser = currentUserEmail != null ? userRepository.findByEmail(currentUserEmail).orElse(null) : null;
        List<Club> clubs = clubRepository.filterClubs(category, search);

        return clubs.stream()
                .map(club -> mapToResponse(club, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClubResponse getClubById(Long clubId, String currentUserEmail) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id: " + clubId));
        User currentUser = currentUserEmail != null ? userRepository.findByEmail(currentUserEmail).orElse(null) : null;
        return mapToResponse(club, currentUser);
    }

    public ClubMembershipStatusResponse joinClub(Long clubId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id: " + clubId));

        if (membershipRepository.existsByClubIdAndUserId(clubId, user.getId())) {
            throw new ClubAlreadyJoinedException("Student has already joined this club.");
        }

        ClubMembership membership = ClubMembership.builder()
                .club(club)
                .user(user)
                .build();

        ClubMembership saved = membershipRepository.save(membership);

        return ClubMembershipStatusResponse.builder()
                .clubId(clubId)
                .joined(true)
                .joinedAt(saved.getJoinedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public ClubMembershipStatusResponse getMembershipStatus(Long clubId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        if (!clubRepository.existsById(clubId)) {
            throw new ResourceNotFoundException("Club not found with id: " + clubId);
        }

        Optional<ClubMembership> membershipOpt = membershipRepository.findByClubIdAndUserId(clubId, user.getId());

        return ClubMembershipStatusResponse.builder()
                .clubId(clubId)
                .joined(membershipOpt.isPresent())
                .joinedAt(membershipOpt.map(ClubMembership::getJoinedAt).orElse(null))
                .build();
    }

    @Transactional(readOnly = true)
    public List<ClubResponse> getMyClubs(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        List<ClubMembership> memberships = membershipRepository.findByUserId(user.getId());

        return memberships.stream()
                .map(m -> mapToResponse(m.getClub(), user))
                .collect(Collectors.toList());
    }

    private ClubResponse mapToResponse(Club club, User currentUser) {
        long count = membershipRepository.countByClubId(club.getId());
        boolean isJoined = false;
        if (currentUser != null) {
            isJoined = membershipRepository.existsByClubIdAndUserId(club.getId(), currentUser.getId());
        }

        return ClubResponse.builder()
                .id(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .category(club.getCategory())
                .presidentName(club.getPresidentName())
                .meetingDay(club.getMeetingDay())
                .meetingTime(club.getMeetingTime())
                .meetingVenue(club.getMeetingVenue())
                .memberCount(count)
                .joined(isJoined)
                .createdAt(club.getCreatedAt())
                .updatedAt(club.getUpdatedAt())
                .build();
    }
}
