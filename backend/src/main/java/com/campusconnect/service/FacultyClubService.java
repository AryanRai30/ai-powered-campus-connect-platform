package com.campusconnect.service;

import com.campusconnect.dto.ClubMemberResponse;
import com.campusconnect.dto.FacultyClubRequest;
import com.campusconnect.dto.FacultyClubResponse;
import com.campusconnect.entity.Club;
import com.campusconnect.entity.ClubMembership;
import com.campusconnect.entity.StudentProfile;
import com.campusconnect.entity.User;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.ClubMembershipRepository;
import com.campusconnect.repository.ClubRepository;
import com.campusconnect.repository.StudentProfileRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacultyClubService {

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public FacultyClubService(
            ClubRepository clubRepository,
            ClubMembershipRepository membershipRepository,
            StudentProfileRepository studentProfileRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.clubRepository = clubRepository;
        this.membershipRepository = membershipRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private User getAuthenticatedFaculty(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        boolean isFaculty = user.getRoles().stream()
                .anyMatch(r -> "FACULTY".equalsIgnoreCase(r.getName()));

        if (!isFaculty) {
            throw new AccessDeniedException("User does not possess FACULTY role.");
        }
        return user;
    }

    private Club getClubAndVerifyOwnership(Long id, User faculty) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id: " + id));

        if (club.getCreatedBy() == null || !club.getCreatedBy().getId().equals(faculty.getId())) {
            throw new AccessDeniedException("Access Denied: You do not have permission to modify this club.");
        }
        return club;
    }

    public FacultyClubResponse createClub(FacultyClubRequest request, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);

        if (clubRepository.existsByName(request.getName().trim())) {
            throw new IllegalArgumentException("A club with this name already exists: " + request.getName());
        }

        Club club = Club.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .category(request.getCategory())
                .presidentName(request.getPresidentName())
                .meetingDay(request.getMeetingDay())
                .meetingTime(request.getMeetingTime())
                .meetingVenue(request.getMeetingVenue())
                .department(request.getDepartment())
                .published(request.getPublished() != null ? request.getPublished() : false)
                .active(true)
                .createdBy(faculty)
                .updatedBy(faculty)
                .build();

        Club saved = clubRepository.save(club);
        if (Boolean.TRUE.equals(saved.getPublished())) {
            notifyTargetedStudentsIfPublished(saved);
        }
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FacultyClubResponse> getFacultyClubs(String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        List<Club> list = clubRepository.findAllByCreatedByOrderByIdDesc(faculty);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FacultyClubResponse getFacultyClubById(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Club club = getClubAndVerifyOwnership(id, faculty);
        return mapToResponse(club);
    }

    public FacultyClubResponse updateClub(Long id, FacultyClubRequest request, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Club club = getClubAndVerifyOwnership(id, faculty);
        boolean wasPublished = Boolean.TRUE.equals(club.getPublished());

        if (!club.getName().equalsIgnoreCase(request.getName().trim()) && clubRepository.existsByName(request.getName().trim())) {
            throw new IllegalArgumentException("A club with this name already exists: " + request.getName());
        }

        club.setName(request.getName().trim());
        club.setDescription(request.getDescription());
        club.setCategory(request.getCategory());
        club.setPresidentName(request.getPresidentName());
        club.setMeetingDay(request.getMeetingDay());
        club.setMeetingTime(request.getMeetingTime());
        club.setMeetingVenue(request.getMeetingVenue());
        club.setDepartment(request.getDepartment());
        if (request.getPublished() != null) {
            club.setPublished(request.getPublished());
        }
        club.setUpdatedBy(faculty);

        Club updated = clubRepository.save(club);
        if (!wasPublished && Boolean.TRUE.equals(updated.getPublished())) {
            notifyTargetedStudentsIfPublished(updated);
        }
        return mapToResponse(updated);
    }

    public void deleteClub(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Club club = getClubAndVerifyOwnership(id, faculty);
        List<ClubMembership> memberships = membershipRepository.findByClubId(club.getId());
        if (!memberships.isEmpty()) {
            membershipRepository.deleteAll(memberships);
        }
        clubRepository.delete(club);
    }

    public FacultyClubResponse publishClub(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Club club = getClubAndVerifyOwnership(id, faculty);
        boolean wasPublished = Boolean.TRUE.equals(club.getPublished());
        club.setPublished(true);
        club.setUpdatedBy(faculty);
        Club updated = clubRepository.save(club);
        if (!wasPublished) {
            notifyTargetedStudentsIfPublished(updated);
        }
        return mapToResponse(updated);
    }

    public FacultyClubResponse unpublishClub(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Club club = getClubAndVerifyOwnership(id, faculty);
        club.setPublished(false);
        club.setUpdatedBy(faculty);
        Club updated = clubRepository.save(club);
        return mapToResponse(updated);
    }

    private void notifyTargetedStudentsIfPublished(Club club) {
        if (club == null || !Boolean.TRUE.equals(club.getPublished())) {
            return;
        }
        List<User> eligibleStudents = userRepository.findStudentUsers(
                true,
                club.getDepartment(),
                null,
                null,
                null,
                null
        );

        Long facultyId = club.getCreatedBy() != null ? club.getCreatedBy().getId() : null;
        List<User> recipients = eligibleStudents.stream()
                .filter(u -> facultyId == null || !facultyId.equals(u.getId()))
                .collect(Collectors.toList());

        if (!recipients.isEmpty()) {
            notificationService.createNotifications(
                    recipients,
                    "New club available",
                    "A new club has been published: " + club.getName(),
                    com.campusconnect.entity.NotificationType.CLUB,
                    "CLUB",
                    club.getId(),
                    "/clubs"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ClubMemberResponse> getClubMembers(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Club club = getClubAndVerifyOwnership(id, faculty);

        List<ClubMembership> memberships = membershipRepository.findByClubId(club.getId());
        return memberships.stream().map(mem -> {
            User studentUser = mem.getUser();
            StudentProfile profile = studentProfileRepository.findByUserId(studentUser.getId()).orElse(null);
            return ClubMemberResponse.builder()
                    .membershipId(mem.getId())
                    .studentId(studentUser.getId())
                    .studentIdCode(profile != null ? profile.getStudentId() : null)
                    .firstName(studentUser.getFirstName())
                    .lastName(studentUser.getLastName())
                    .email(studentUser.getEmail())
                    .course(profile != null ? profile.getCourse() : null)
                    .department(profile != null ? profile.getDepartment() : null)
                    .year(profile != null ? profile.getYear() : null)
                    .semester(profile != null ? profile.getSemester() : null)
                    .joinedAt(mem.getJoinedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    private FacultyClubResponse mapToResponse(Club c) {
        long memberCount = membershipRepository.countByClubId(c.getId());
        return FacultyClubResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .category(c.getCategory())
                .presidentName(c.getPresidentName())
                .meetingDay(c.getMeetingDay())
                .meetingTime(c.getMeetingTime())
                .meetingVenue(c.getMeetingVenue())
                .department(c.getDepartment())
                .memberCount(memberCount)
                .published(c.getPublished())
                .active(c.getActive())
                .createdByEmail(c.getCreatedBy() != null ? c.getCreatedBy().getEmail() : null)
                .createdByName(c.getCreatedBy() != null ? c.getCreatedBy().getFirstName() + " " + c.getCreatedBy().getLastName() : null)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
