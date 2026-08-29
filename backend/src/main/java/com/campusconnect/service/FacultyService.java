package com.campusconnect.service;

import com.campusconnect.dto.FacultyDashboardResponse;
import com.campusconnect.dto.FacultyDashboardStatsResponse;
import com.campusconnect.dto.FacultyStudentResponse;
import com.campusconnect.entity.Club;
import com.campusconnect.entity.Event;
import com.campusconnect.entity.Opportunity;
import com.campusconnect.entity.StudentProfile;
import com.campusconnect.entity.User;
import com.campusconnect.repository.AcademicResourceRepository;
import com.campusconnect.repository.AnnouncementRepository;
import com.campusconnect.repository.ClubMembershipRepository;
import com.campusconnect.repository.ClubRepository;
import com.campusconnect.repository.EventRegistrationRepository;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.repository.OpportunityApplicationRepository;
import com.campusconnect.repository.OpportunityRepository;
import com.campusconnect.repository.StudentProfileRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling operations related to authenticated Faculty users.
 */
@Service
public class FacultyService {

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final AcademicResourceRepository resourceRepository;
    private final AnnouncementRepository announcementRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final OpportunityRepository opportunityRepository;
    private final OpportunityApplicationRepository applicationRepository;
    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;

    public FacultyService(
            UserRepository userRepository,
            StudentProfileRepository profileRepository,
            AcademicResourceRepository resourceRepository,
            AnnouncementRepository announcementRepository,
            EventRepository eventRepository,
            EventRegistrationRepository registrationRepository,
            OpportunityRepository opportunityRepository,
            OpportunityApplicationRepository applicationRepository,
            ClubRepository clubRepository,
            ClubMembershipRepository membershipRepository
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.resourceRepository = resourceRepository;
        this.announcementRepository = announcementRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.opportunityRepository = opportunityRepository;
        this.applicationRepository = applicationRepository;
        this.clubRepository = clubRepository;
        this.membershipRepository = membershipRepository;
    }

    private User getAuthenticatedFaculty(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("Faculty user not found with email: " + email));

        boolean isFaculty = user.getRoles().stream()
                .anyMatch(r -> "FACULTY".equalsIgnoreCase(r.getName()));

        if (!isFaculty) {
            throw new AccessDeniedException("User does not possess FACULTY role.");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public FacultyDashboardResponse getFacultyDashboard(String email) {
        User user = getAuthenticatedFaculty(email);

        return FacultyDashboardResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role("FACULTY")
                .status(user.isActive() ? "ACTIVE" : "INACTIVE")
                .build();
    }

    @Transactional(readOnly = true)
    public FacultyDashboardStatsResponse getFacultyStats(String email) {
        User faculty = getAuthenticatedFaculty(email);

        long resourceCount = resourceRepository.countByCreatedBy(faculty);
        long announcementCount = announcementRepository.countByCreatedBy(faculty);
        long eventCount = eventRepository.countByCreatedBy(faculty);
        long opportunityCount = opportunityRepository.countByCreatedBy(faculty);
        long clubCount = clubRepository.countByCreatedBy(faculty);

        List<Event> myEvents = eventRepository.findAllByCreatedByOrderByIdDesc(faculty);
        long totalRegistrations = myEvents.stream()
                .mapToLong(e -> registrationRepository.countByEventId(e.getId()))
                .sum();

        List<Club> myClubs = clubRepository.findAllByCreatedByOrderByIdDesc(faculty);
        long totalMembers = myClubs.stream()
                .mapToLong(c -> membershipRepository.countByClubId(c.getId()))
                .sum();

        List<Opportunity> myOpps = opportunityRepository.findAllByCreatedByOrderByIdDesc(faculty);
        long totalApplications = myOpps.stream()
                .mapToLong(o -> applicationRepository.countByOpportunityId(o.getId()))
                .sum();

        return FacultyDashboardStatsResponse.builder()
                .resourceCount(resourceCount)
                .announcementCount(announcementCount)
                .eventCount(eventCount)
                .opportunityCount(opportunityCount)
                .clubCount(clubCount)
                .totalEventRegistrations(totalRegistrations)
                .totalClubMembers(totalMembers)
                .totalOpportunityApplications(totalApplications)
                .build();
    }

    @Transactional(readOnly = true)
    public List<FacultyStudentResponse> getFacultyStudents(
            String email,
            String department,
            String course,
            String year,
            String semester,
            String search
    ) {
        getAuthenticatedFaculty(email);

        List<StudentProfile> profiles = profileRepository.filterStudents(department, course, year, semester, search);

        return profiles.stream().map(sp -> {
            User u = sp.getUser();
            return FacultyStudentResponse.builder()
                    .id(u.getId())
                    .studentId(sp.getStudentId())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .email(u.getEmail())
                    .course(sp.getCourse())
                    .department(sp.getDepartment())
                    .year(sp.getYear())
                    .semester(sp.getSemester())
                    .build();
        }).collect(Collectors.toList());
    }
}
