package com.campusconnect.service;

import com.campusconnect.dto.FacultyOpportunityRequest;
import com.campusconnect.dto.FacultyOpportunityResponse;
import com.campusconnect.entity.Opportunity;
import com.campusconnect.entity.User;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.OpportunityRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacultyOpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final com.campusconnect.repository.OpportunityApplicationRepository applicationRepository;
    private final com.campusconnect.repository.OpportunityBookmarkRepository bookmarkRepository;
    private final com.campusconnect.repository.StudentProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public FacultyOpportunityService(
            OpportunityRepository opportunityRepository,
            com.campusconnect.repository.OpportunityApplicationRepository applicationRepository,
            com.campusconnect.repository.OpportunityBookmarkRepository bookmarkRepository,
            com.campusconnect.repository.StudentProfileRepository profileRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.opportunityRepository = opportunityRepository;
        this.applicationRepository = applicationRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.profileRepository = profileRepository;
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

    private Opportunity getOpportunityAndVerifyOwnership(Long id, User faculty) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));

        if (opportunity.getCreatedBy() == null || !opportunity.getCreatedBy().getId().equals(faculty.getId())) {
            throw new AccessDeniedException("Access Denied: You do not have permission to modify this opportunity.");
        }
        return opportunity;
    }

    public FacultyOpportunityResponse createOpportunity(FacultyOpportunityRequest request, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);

        Opportunity opportunity = Opportunity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .organization(request.getOrganization())
                .opportunityType(request.getOpportunityType())
                .location(request.getLocation())
                .skills(request.getSkills())
                .deadline(request.getDeadline())
                .applicationUrl(request.getApplicationUrl())
                .eligibility(request.getEligibility())
                .targetDepartment(request.getTargetDepartment())
                .targetCourse(request.getTargetCourse())
                .targetYear(request.getTargetYear())
                .targetSemester(request.getTargetSemester())
                .published(request.getPublished() != null ? request.getPublished() : false)
                .active(true)
                .createdBy(faculty)
                .build();

        Opportunity saved = opportunityRepository.save(opportunity);
        if (Boolean.TRUE.equals(saved.getPublished())) {
            notifyTargetedStudentsIfPublished(saved);
        }
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FacultyOpportunityResponse> getFacultyOpportunities(String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        List<Opportunity> list = opportunityRepository.findAllByCreatedByOrderByIdDesc(faculty);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FacultyOpportunityResponse getFacultyOpportunityById(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Opportunity opportunity = getOpportunityAndVerifyOwnership(id, faculty);
        return mapToResponse(opportunity);
    }

    public FacultyOpportunityResponse updateOpportunity(Long id, FacultyOpportunityRequest request, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Opportunity opportunity = getOpportunityAndVerifyOwnership(id, faculty);
        boolean wasPublished = Boolean.TRUE.equals(opportunity.getPublished());

        opportunity.setTitle(request.getTitle());
        opportunity.setDescription(request.getDescription());
        opportunity.setOrganization(request.getOrganization());
        opportunity.setOpportunityType(request.getOpportunityType());
        opportunity.setLocation(request.getLocation());
        opportunity.setSkills(request.getSkills());
        opportunity.setDeadline(request.getDeadline());
        opportunity.setApplicationUrl(request.getApplicationUrl());
        opportunity.setEligibility(request.getEligibility());
        opportunity.setTargetDepartment(request.getTargetDepartment());
        opportunity.setTargetCourse(request.getTargetCourse());
        opportunity.setTargetYear(request.getTargetYear());
        opportunity.setTargetSemester(request.getTargetSemester());
        if (request.getPublished() != null) {
            opportunity.setPublished(request.getPublished());
        }

        Opportunity updated = opportunityRepository.save(opportunity);
        if (!wasPublished && Boolean.TRUE.equals(updated.getPublished())) {
            notifyTargetedStudentsIfPublished(updated);
        }
        return mapToResponse(updated);
    }

    public void deleteOpportunity(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Opportunity opportunity = getOpportunityAndVerifyOwnership(id, faculty);

        List<com.campusconnect.entity.OpportunityApplication> apps = applicationRepository.findByOpportunityId(opportunity.getId());
        if (!apps.isEmpty()) {
            applicationRepository.deleteAll(apps);
        }

        List<com.campusconnect.entity.OpportunityBookmark> bookmarks = bookmarkRepository.findByOpportunityId(opportunity.getId());
        if (!bookmarks.isEmpty()) {
            bookmarkRepository.deleteAll(bookmarks);
        }

        opportunityRepository.delete(opportunity);
    }

    public FacultyOpportunityResponse publishOpportunity(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Opportunity opportunity = getOpportunityAndVerifyOwnership(id, faculty);
        boolean wasPublished = Boolean.TRUE.equals(opportunity.getPublished());
        opportunity.setPublished(true);
        Opportunity updated = opportunityRepository.save(opportunity);
        if (!wasPublished) {
            notifyTargetedStudentsIfPublished(updated);
        }
        return mapToResponse(updated);
    }

    public FacultyOpportunityResponse unpublishOpportunity(Long id, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Opportunity opportunity = getOpportunityAndVerifyOwnership(id, faculty);
        opportunity.setPublished(false);
        Opportunity updated = opportunityRepository.save(opportunity);
        return mapToResponse(updated);
    }

    private void notifyTargetedStudentsIfPublished(Opportunity opportunity) {
        if (opportunity == null || !Boolean.TRUE.equals(opportunity.getPublished())) {
            return;
        }
        String yearStr = opportunity.getTargetYear() != null ? String.valueOf(opportunity.getTargetYear()) : null;
        String semStr = opportunity.getTargetSemester() != null ? String.valueOf(opportunity.getTargetSemester()) : null;

        List<User> eligibleStudents = userRepository.findStudentUsers(
                true,
                opportunity.getTargetDepartment(),
                opportunity.getTargetCourse(),
                yearStr,
                semStr,
                null
        );

        Long facultyId = opportunity.getCreatedBy() != null ? opportunity.getCreatedBy().getId() : null;
        List<User> recipients = eligibleStudents.stream()
                .filter(u -> facultyId == null || !facultyId.equals(u.getId()))
                .collect(Collectors.toList());

        if (!recipients.isEmpty()) {
            notificationService.createNotifications(
                    recipients,
                    "New opportunity available",
                    "A new opportunity has been published: " + opportunity.getTitle(),
                    com.campusconnect.entity.NotificationType.OPPORTUNITY,
                    "OPPORTUNITY",
                    opportunity.getId(),
                    "/opportunities"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<com.campusconnect.dto.FacultyOpportunityApplicationResponse> getOpportunityApplications(Long opportunityId, String facultyEmail) {
        User faculty = getAuthenticatedFaculty(facultyEmail);
        Opportunity opportunity = getOpportunityAndVerifyOwnership(opportunityId, faculty);

        List<com.campusconnect.entity.OpportunityApplication> apps = applicationRepository.findByOpportunityId(opportunity.getId());

        return apps.stream().map(app -> {
            User studentUser = app.getUser();
            com.campusconnect.entity.StudentProfile profile = profileRepository.findByUserId(studentUser.getId()).orElse(null);

            return com.campusconnect.dto.FacultyOpportunityApplicationResponse.builder()
                    .applicationId(app.getId())
                    .opportunityId(opportunity.getId())
                    .opportunityTitle(opportunity.getTitle())
                    .studentUserId(studentUser.getId())
                    .studentId(profile != null ? profile.getStudentId() : null)
                    .firstName(studentUser.getFirstName())
                    .lastName(studentUser.getLastName())
                    .email(studentUser.getEmail())
                    .course(profile != null ? profile.getCourse() : null)
                    .department(profile != null ? profile.getDepartment() : null)
                    .year(profile != null ? profile.getYear() : null)
                    .semester(profile != null ? profile.getSemester() : null)
                    .applicationStatus(app.getStatus())
                    .appliedAt(app.getAppliedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    private FacultyOpportunityResponse mapToResponse(Opportunity o) {
        return FacultyOpportunityResponse.builder()
                .id(o.getId())
                .title(o.getTitle())
                .description(o.getDescription())
                .organization(o.getOrganization())
                .opportunityType(o.getOpportunityType())
                .location(o.getLocation())
                .skills(o.getSkills())
                .deadline(o.getDeadline())
                .applicationUrl(o.getApplicationUrl())
                .eligibility(o.getEligibility())
                .published(o.getPublished())
                .active(o.getActive())
                .targetDepartment(o.getTargetDepartment())
                .targetCourse(o.getTargetCourse())
                .targetYear(o.getTargetYear())
                .targetSemester(o.getTargetSemester())
                .createdByEmail(o.getCreatedBy() != null ? o.getCreatedBy().getEmail() : null)
                .createdByName(o.getCreatedBy() != null ? o.getCreatedBy().getFirstName() + " " + o.getCreatedBy().getLastName() : null)
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
