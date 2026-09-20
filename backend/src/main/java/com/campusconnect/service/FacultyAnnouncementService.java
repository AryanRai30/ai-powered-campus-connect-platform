package com.campusconnect.service;

import com.campusconnect.dto.FacultyAnnouncementRequest;
import com.campusconnect.dto.FacultyAnnouncementResponse;
import com.campusconnect.entity.Announcement;
import com.campusconnect.entity.User;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.AnnouncementRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacultyAnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public FacultyAnnouncementService(
            AnnouncementRepository announcementRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.announcementRepository = announcementRepository;
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

    private Announcement getAnnouncementAndVerifyOwnership(Long id, User faculty) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));

        if (announcement.getCreatedBy() == null || !announcement.getCreatedBy().getId().equals(faculty.getId())) {
            throw new AccessDeniedException("Access Denied: You do not have permission to modify this announcement.");
        }
        return announcement;
    }

    public FacultyAnnouncementResponse createAnnouncement(FacultyAnnouncementRequest request, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);

        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .targetDepartment(request.getTargetDepartment())
                .targetCourse(request.getTargetCourse())
                .targetYear(request.getTargetYear())
                .targetSemester(request.getTargetSemester())
                .published(request.getPublished() != null ? request.getPublished() : false)
                .active(true)
                .publishedAt(LocalDateTime.now())
                .createdBy(faculty)
                .build();

        Announcement saved = announcementRepository.save(announcement);
        if (Boolean.TRUE.equals(saved.getPublished())) {
            notifyTargetedStudentsIfPublished(saved);
        }
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FacultyAnnouncementResponse> getFacultyAnnouncements(String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        List<Announcement> list = announcementRepository.findAllByCreatedByOrderByIdDesc(faculty);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FacultyAnnouncementResponse getFacultyAnnouncementById(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Announcement announcement = getAnnouncementAndVerifyOwnership(id, faculty);
        return mapToResponse(announcement);
    }

    public FacultyAnnouncementResponse updateAnnouncement(Long id, FacultyAnnouncementRequest request, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Announcement announcement = getAnnouncementAndVerifyOwnership(id, faculty);
        boolean wasPublished = Boolean.TRUE.equals(announcement.getPublished());

        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setCategory(request.getCategory());
        announcement.setTargetDepartment(request.getTargetDepartment());
        announcement.setTargetCourse(request.getTargetCourse());
        announcement.setTargetYear(request.getTargetYear());
        announcement.setTargetSemester(request.getTargetSemester());
        if (request.getPublished() != null) {
            announcement.setPublished(request.getPublished());
        }

        Announcement updated = announcementRepository.save(announcement);
        if (!wasPublished && Boolean.TRUE.equals(updated.getPublished())) {
            notifyTargetedStudentsIfPublished(updated);
        }
        return mapToResponse(updated);
    }

    public void deleteAnnouncement(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Announcement announcement = getAnnouncementAndVerifyOwnership(id, faculty);
        announcementRepository.delete(announcement);
    }

    public FacultyAnnouncementResponse publishAnnouncement(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Announcement announcement = getAnnouncementAndVerifyOwnership(id, faculty);
        boolean wasPublished = Boolean.TRUE.equals(announcement.getPublished());
        announcement.setPublished(true);
        announcement.setPublishedAt(LocalDateTime.now());
        Announcement updated = announcementRepository.save(announcement);
        if (!wasPublished) {
            notifyTargetedStudentsIfPublished(updated);
        }
        return mapToResponse(updated);
    }

    public FacultyAnnouncementResponse unpublishAnnouncement(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Announcement announcement = getAnnouncementAndVerifyOwnership(id, faculty);
        announcement.setPublished(false);
        Announcement updated = announcementRepository.save(announcement);
        return mapToResponse(updated);
    }

    private void notifyTargetedStudentsIfPublished(Announcement announcement) {
        if (announcement == null || !Boolean.TRUE.equals(announcement.getPublished())) {
            return;
        }
        String yearStr = announcement.getTargetYear() != null ? String.valueOf(announcement.getTargetYear()) : null;
        String semStr = announcement.getTargetSemester() != null ? String.valueOf(announcement.getTargetSemester()) : null;

        List<User> eligibleStudents = userRepository.findStudentUsers(
                true,
                announcement.getTargetDepartment(),
                announcement.getTargetCourse(),
                yearStr,
                semStr,
                null
        );

        Long facultyId = announcement.getCreatedBy() != null ? announcement.getCreatedBy().getId() : null;
        List<User> recipients = eligibleStudents.stream()
                .filter(u -> facultyId == null || !facultyId.equals(u.getId()))
                .collect(Collectors.toList());

        if (!recipients.isEmpty()) {
            notificationService.createNotifications(
                    recipients,
                    "New announcement",
                    "A new announcement has been published: " + announcement.getTitle(),
                    com.campusconnect.entity.NotificationType.ANNOUNCEMENT,
                    "ANNOUNCEMENT",
                    announcement.getId(),
                    "/announcements"
            );
        }
    }

    private FacultyAnnouncementResponse mapToResponse(Announcement a) {
        return FacultyAnnouncementResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .category(a.getCategory())
                .published(a.getPublished())
                .active(a.getActive())
                .targetDepartment(a.getTargetDepartment())
                .targetCourse(a.getTargetCourse())
                .targetYear(a.getTargetYear())
                .targetSemester(a.getTargetSemester())
                .createdByEmail(a.getCreatedBy() != null ? a.getCreatedBy().getEmail() : null)
                .createdByName(a.getCreatedBy() != null ? a.getCreatedBy().getFirstName() + " " + a.getCreatedBy().getLastName() : null)
                .publishedAt(a.getPublishedAt())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
