package com.campusconnect.service;

import com.campusconnect.dto.*;
import com.campusconnect.entity.*;
import com.campusconnect.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service providing real database activity and analytics metrics for Students, Faculty, and Admin.
 */
@Service
@Transactional(readOnly = true)
public class ActivityAnalyticsService {

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final AcademicResourceRepository resourceRepository;
    private final AnnouncementRepository announcementRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final OpportunityRepository opportunityRepository;
    private final OpportunityApplicationRepository applicationRepository;
    private final OpportunityBookmarkRepository bookmarkRepository;

    public ActivityAnalyticsService(
            UserRepository userRepository,
            StudentProfileRepository profileRepository,
            AcademicResourceRepository resourceRepository,
            AnnouncementRepository announcementRepository,
            EventRepository eventRepository,
            EventRegistrationRepository registrationRepository,
            ClubRepository clubRepository,
            ClubMembershipRepository membershipRepository,
            OpportunityRepository opportunityRepository,
            OpportunityApplicationRepository applicationRepository,
            OpportunityBookmarkRepository bookmarkRepository
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.resourceRepository = resourceRepository;
        this.announcementRepository = announcementRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.clubRepository = clubRepository;
        this.membershipRepository = membershipRepository;
        this.opportunityRepository = opportunityRepository;
        this.applicationRepository = applicationRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public StudentActivityAnalyticsResponse getStudentActivityAnalytics(String email) {
        User user = getUserByEmail(email);

        List<EventRegistration> registrations = registrationRepository.findByUserIdOrderByRegisteredAtDesc(user.getId());
        List<ClubMembership> memberships = membershipRepository.findByUserId(user.getId());
        List<OpportunityApplication> applications = applicationRepository.findByUserIdOrderByAppliedAtDesc(user.getId());
        List<OpportunityBookmark> bookmarks = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        StudentProfile sp = profileRepository.findByUserId(user.getId()).orElse(null);
        String targetDept = sp != null ? sp.getDepartment() : null;
        String targetCourse = sp != null ? sp.getCourse() : null;
        Integer targetYear = null;
        Integer targetSem = null;
        if (sp != null) {
            try { targetYear = Integer.parseInt(sp.getYear()); } catch (Exception ignored) {}
            try { targetSem = Integer.parseInt(sp.getSemester()); } catch (Exception ignored) {}
        }

        long accessibleResources = resourceRepository.filterResourcesForStudent(
                null, null, null, targetDept, targetCourse, targetYear, targetSem, null).size();

        // Convert to DTO lists
        List<EventResponse> registeredEvents = registrations.stream().map(reg -> {
            Event e = reg.getEvent();
            return EventResponse.builder()
                    .id(e.getId())
                    .title(e.getTitle())
                    .description(e.getDescription())
                    .eventDate(e.getEventDate())
                    .eventTime(e.getEventTime())
                    .venue(e.getVenue())
                    .category(e.getCategory())
                    .organizer(e.getOrganizer())
                    .registrationRequired(e.isRegistrationRequired())
                    .registered(true)
                    .createdAt(e.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        List<ClubResponse> joinedClubs = memberships.stream().map(mem -> {
            Club c = mem.getClub();
            return ClubResponse.builder()
                    .id(c.getId())
                    .name(c.getName())
                    .description(c.getDescription())
                    .category(c.getCategory())
                    .presidentName(c.getPresidentName())
                    .joined(true)
                    .createdAt(c.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        List<OpportunityResponse> appResponses = applications.stream().map(app -> {
            Opportunity o = app.getOpportunity();
            return OpportunityResponse.builder()
                    .id(o.getId())
                    .title(o.getTitle())
                    .description(o.getDescription())
                    .organization(o.getOrganization())
                    .opportunityType(o.getOpportunityType())
                    .location(o.getLocation())
                    .skills(o.getSkills())
                    .deadline(o.getDeadline())
                    .applied(true)
                    .applicationStatus(app.getStatus())
                    .createdAt(o.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        List<OpportunityResponse> bmResponses = bookmarks.stream().map(bm -> {
            Opportunity o = bm.getOpportunity();
            return OpportunityResponse.builder()
                    .id(o.getId())
                    .title(o.getTitle())
                    .description(o.getDescription())
                    .organization(o.getOrganization())
                    .opportunityType(o.getOpportunityType())
                    .location(o.getLocation())
                    .skills(o.getSkills())
                    .deadline(o.getDeadline())
                    .bookmarked(true)
                    .createdAt(o.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        // Timeline items
        List<ActivityLogItemDto> activities = new ArrayList<>();

        for (EventRegistration reg : registrations) {
            activities.add(ActivityLogItemDto.builder()
                    .type("EVENT_REGISTRATION")
                    .title(reg.getEvent().getTitle())
                    .description("Registered for event at " + reg.getEvent().getVenue())
                    .timestamp(reg.getRegisteredAt())
                    .referenceId(reg.getEvent().getId())
                    .build());
        }

        for (ClubMembership mem : memberships) {
            activities.add(ActivityLogItemDto.builder()
                    .type("CLUB_JOIN")
                    .title(mem.getClub().getName())
                    .description("Joined " + (mem.getClub().getCategory() != null ? mem.getClub().getCategory() : "Campus") + " Club")
                    .timestamp(mem.getJoinedAt())
                    .referenceId(mem.getClub().getId())
                    .build());
        }

        for (OpportunityApplication app : applications) {
            activities.add(ActivityLogItemDto.builder()
                    .type("OPPORTUNITY_APPLY")
                    .title(app.getOpportunity().getTitle())
                    .description("Applied to " + app.getOpportunity().getOrganization() + " (" + app.getStatus() + ")")
                    .timestamp(app.getAppliedAt())
                    .referenceId(app.getOpportunity().getId())
                    .build());
        }

        for (OpportunityBookmark bm : bookmarks) {
            activities.add(ActivityLogItemDto.builder()
                    .type("OPPORTUNITY_BOOKMARK")
                    .title(bm.getOpportunity().getTitle())
                    .description("Saved opportunity from " + bm.getOpportunity().getOrganization())
                    .timestamp(bm.getCreatedAt())
                    .referenceId(bm.getOpportunity().getId())
                    .build());
        }

        activities.sort(Comparator.comparing(ActivityLogItemDto::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())));

        return StudentActivityAnalyticsResponse.builder()
                .totalEventRegistrations(registrations.size())
                .totalClubMemberships(memberships.size())
                .totalOpportunityBookmarks(bookmarks.size())
                .totalOpportunityApplications(applications.size())
                .totalAccessibleResources(accessibleResources)
                .recentActivities(activities)
                .registeredEvents(registeredEvents)
                .joinedClubs(joinedClubs)
                .applications(appResponses)
                .bookmarks(bmResponses)
                .build();
    }

    public FacultyActivityAnalyticsResponse getFacultyActivityAnalytics(String email) {
        User faculty = getUserByEmail(email);

        boolean isFaculty = faculty.getRoles().stream().anyMatch(r -> "FACULTY".equalsIgnoreCase(r.getName()));
        if (!isFaculty) {
            throw new AccessDeniedException("User does not possess FACULTY role.");
        }

        long resourceCount = resourceRepository.countByCreatedBy(faculty);
        long eventCount = eventRepository.countByCreatedBy(faculty);
        long announcementCount = announcementRepository.countByCreatedBy(faculty);
        long opportunityCount = opportunityRepository.countByCreatedBy(faculty);
        long clubCount = clubRepository.countByCreatedBy(faculty);

        List<Event> myEvents = eventRepository.findAllByCreatedByOrderByIdDesc(faculty);
        List<EntityEngagementSummaryDto> eventBreakdown = myEvents.stream().map(e -> {
            long regCount = registrationRepository.countByEventId(e.getId());
            return EntityEngagementSummaryDto.builder()
                    .id(e.getId())
                    .title(e.getTitle())
                    .subTitle(e.getVenue() + " • " + e.getEventDate())
                    .entityType("EVENT")
                    .totalEngagement(regCount)
                    .build();
        }).collect(Collectors.toList());
        long totalEventRegs = eventBreakdown.stream().mapToLong(EntityEngagementSummaryDto::getTotalEngagement).sum();

        List<Opportunity> myOpps = opportunityRepository.findAllByCreatedByOrderByIdDesc(faculty);
        List<EntityEngagementSummaryDto> oppBreakdown = myOpps.stream().map(o -> {
            long appCount = applicationRepository.countByOpportunityId(o.getId());
            return EntityEngagementSummaryDto.builder()
                    .id(o.getId())
                    .title(o.getTitle())
                    .subTitle(o.getOrganization() + " (" + o.getOpportunityType() + ")")
                    .entityType("OPPORTUNITY")
                    .totalEngagement(appCount)
                    .build();
        }).collect(Collectors.toList());
        long totalOppApps = oppBreakdown.stream().mapToLong(EntityEngagementSummaryDto::getTotalEngagement).sum();

        List<Club> myClubs = clubRepository.findAllByCreatedByOrderByIdDesc(faculty);
        List<EntityEngagementSummaryDto> clubBreakdown = myClubs.stream().map(c -> {
            long memCount = membershipRepository.countByClubId(c.getId());
            return EntityEngagementSummaryDto.builder()
                    .id(c.getId())
                    .title(c.getName())
                    .subTitle(c.getCategory() != null ? c.getCategory() : "Club")
                    .entityType("CLUB")
                    .totalEngagement(memCount)
                    .build();
        }).collect(Collectors.toList());
        long totalClubMems = clubBreakdown.stream().mapToLong(EntityEngagementSummaryDto::getTotalEngagement).sum();

        // Recent content creation items
        List<ContentActivityItemDto> recentContent = new ArrayList<>();

        for (AcademicResource r : resourceRepository.findAllByCreatedByOrderByIdDesc(faculty)) {
            recentContent.add(ContentActivityItemDto.builder()
                    .id(r.getId())
                    .contentType("RESOURCE")
                    .title(r.getTitle())
                    .category(r.getCategory())
                    .status(Boolean.TRUE.equals(r.getPublished()) ? "PUBLISHED" : "DRAFT")
                    .createdAt(r.getCreatedAt())
                    .engagementCount(0)
                    .build());
        }

        for (Event e : myEvents) {
            long regCount = registrationRepository.countByEventId(e.getId());
            recentContent.add(ContentActivityItemDto.builder()
                    .id(e.getId())
                    .contentType("EVENT")
                    .title(e.getTitle())
                    .category(e.getCategory())
                    .status(Boolean.TRUE.equals(e.getPublished()) ? "PUBLISHED" : "DRAFT")
                    .createdAt(e.getCreatedAt())
                    .engagementCount(regCount)
                    .build());
        }

        for (Announcement a : announcementRepository.findAll()) {
            if (a.getCreatedBy() != null && a.getCreatedBy().getId().equals(faculty.getId())) {
                recentContent.add(ContentActivityItemDto.builder()
                        .id(a.getId())
                        .contentType("ANNOUNCEMENT")
                        .title(a.getTitle())
                        .category(a.getCategory())
                        .status(Boolean.TRUE.equals(a.getPublished()) ? "PUBLISHED" : "DRAFT")
                        .createdAt(a.getCreatedAt())
                        .engagementCount(0)
                        .build());
            }
        }

        for (Opportunity o : myOpps) {
            long appCount = applicationRepository.countByOpportunityId(o.getId());
            recentContent.add(ContentActivityItemDto.builder()
                    .id(o.getId())
                    .contentType("OPPORTUNITY")
                    .title(o.getTitle())
                    .category(o.getOpportunityType())
                    .status(Boolean.TRUE.equals(o.getPublished()) ? "PUBLISHED" : "DRAFT")
                    .createdAt(o.getCreatedAt())
                    .engagementCount(appCount)
                    .build());
        }

        recentContent.sort(Comparator.comparing(ContentActivityItemDto::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        return FacultyActivityAnalyticsResponse.builder()
                .publishedResourcesCount(resourceCount)
                .eventsCreatedCount(eventCount)
                .announcementsCreatedCount(announcementCount)
                .opportunitiesCreatedCount(opportunityCount)
                .clubsSupervisedCount(clubCount)
                .totalEventRegistrationsReceived(totalEventRegs)
                .totalClubMembersReceived(totalClubMems)
                .totalOpportunityApplicationsReceived(totalOppApps)
                .recentContent(recentContent)
                .eventEngagementBreakdown(eventBreakdown)
                .opportunityEngagementBreakdown(oppBreakdown)
                .clubEngagementBreakdown(clubBreakdown)
                .build();
    }

    public AdminPlatformAnalyticsResponse getAdminPlatformAnalytics(String email) {
        User admin = getUserByEmail(email);

        boolean isAdmin = admin.getRoles().stream().anyMatch(r ->
                "ADMIN".equalsIgnoreCase(r.getName()) ||
                "SUPER_ADMIN".equalsIgnoreCase(r.getName()) ||
                "CLUB_ADMIN".equalsIgnoreCase(r.getName())
        );
        if (!isAdmin) {
            throw new AccessDeniedException("User does not possess ADMIN privileges.");
        }

        long totalStudents = userRepository.countByRoleName("STUDENT");
        long totalFaculty = userRepository.countByRoleName("FACULTY");
        long totalAdmins = userRepository.countByRoleNameIn(List.of("ADMIN", "SUPER_ADMIN", "CLUB_ADMIN"));

        long publishedResources = resourceRepository.countByPublishedTrueAndActiveTrue();
        long totalResources = resourceRepository.count();

        long eventsCount = eventRepository.count();
        long publishedEventsCount = eventRepository.countByPublishedTrueAndActiveTrue();
        long totalEventRegistrations = registrationRepository.count();

        long clubsCount = clubRepository.count();
        long totalClubMemberships = membershipRepository.count();

        long opportunitiesCount = opportunityRepository.count();
        long publishedOpportunitiesCount = opportunityRepository.countByPublishedTrueAndActiveTrue();
        long totalOpportunityApplications = applicationRepository.count();
        long totalOpportunityBookmarks = bookmarkRepository.count();

        long announcementsCount = announcementRepository.count();

        // Department breakdown
        List<DepartmentStatDto> deptStats = new ArrayList<>();
        List<Object[]> deptCounts = profileRepository.countStudentsByDepartment();
        for (Object[] row : deptCounts) {
            String dept = row[0] != null ? row[0].toString() : "Unassigned";
            long cnt = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
            deptStats.add(DepartmentStatDto.builder().department(dept).studentCount(cnt).build());
        }

        // Global platform activity timeline
        List<ActivityLogItemDto> recentActivities = new ArrayList<>();

        List<EventRegistration> allRegs = registrationRepository.findAll();
        for (EventRegistration reg : allRegs) {
            String studentName = reg.getUser() != null ? reg.getUser().getFirstName() + " " + reg.getUser().getLastName() : "Student";
            recentActivities.add(ActivityLogItemDto.builder()
                    .type("EVENT_REGISTRATION")
                    .title(reg.getEvent() != null ? reg.getEvent().getTitle() : "Event")
                    .description(studentName + " registered for event")
                    .timestamp(reg.getRegisteredAt())
                    .referenceId(reg.getId())
                    .build());
        }

        List<OpportunityApplication> allApps = applicationRepository.findAll();
        for (OpportunityApplication app : allApps) {
            String studentName = app.getUser() != null ? app.getUser().getFirstName() + " " + app.getUser().getLastName() : "Student";
            recentActivities.add(ActivityLogItemDto.builder()
                    .type("OPPORTUNITY_APPLY")
                    .title(app.getOpportunity() != null ? app.getOpportunity().getTitle() : "Opportunity")
                    .description(studentName + " applied (" + app.getStatus() + ")")
                    .timestamp(app.getAppliedAt())
                    .referenceId(app.getId())
                    .build());
        }

        List<ClubMembership> allMems = membershipRepository.findAll();
        for (ClubMembership mem : allMems) {
            String studentName = mem.getUser() != null ? mem.getUser().getFirstName() + " " + mem.getUser().getLastName() : "Student";
            recentActivities.add(ActivityLogItemDto.builder()
                    .type("CLUB_JOIN")
                    .title(mem.getClub() != null ? mem.getClub().getName() : "Club")
                    .description(studentName + " joined club")
                    .timestamp(mem.getJoinedAt())
                    .referenceId(mem.getId())
                    .build());
        }

        recentActivities.sort(Comparator.comparing(ActivityLogItemDto::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())));
        if (recentActivities.size() > 25) {
            recentActivities = recentActivities.subList(0, 25);
        }

        return AdminPlatformAnalyticsResponse.builder()
                .totalStudents(totalStudents)
                .totalFaculty(totalFaculty)
                .totalAdmins(totalAdmins)
                .publishedResourcesCount(publishedResources)
                .totalResourcesCount(totalResources)
                .eventsCount(eventsCount)
                .publishedEventsCount(publishedEventsCount)
                .totalEventRegistrations(totalEventRegistrations)
                .clubsCount(clubsCount)
                .totalClubMemberships(totalClubMemberships)
                .opportunitiesCount(opportunitiesCount)
                .publishedOpportunitiesCount(publishedOpportunitiesCount)
                .totalOpportunityApplications(totalOpportunityApplications)
                .totalOpportunityBookmarks(totalOpportunityBookmarks)
                .announcementsCount(announcementsCount)
                .departmentDistribution(deptStats)
                .recentPlatformActivities(recentActivities)
                .build();
    }
}
