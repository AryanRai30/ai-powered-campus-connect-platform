package com.campusconnect.service;

import com.campusconnect.dto.AdminStatsResponse;
import com.campusconnect.entity.User;
import com.campusconnect.repository.AcademicResourceRepository;
import com.campusconnect.repository.AnnouncementRepository;
import com.campusconnect.repository.ClubRepository;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.repository.OpportunityRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service handling system overview administration statistics and operations.
 */
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final AnnouncementRepository announcementRepository;
    private final ClubRepository clubRepository;
    private final AcademicResourceRepository resourceRepository;
    private final OpportunityRepository opportunityRepository;

    public AdminService(
            UserRepository userRepository,
            EventRepository eventRepository,
            AnnouncementRepository announcementRepository,
            ClubRepository clubRepository,
            AcademicResourceRepository resourceRepository,
            OpportunityRepository opportunityRepository
    ) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.announcementRepository = announcementRepository;
        this.clubRepository = clubRepository;
        this.resourceRepository = resourceRepository;
        this.opportunityRepository = opportunityRepository;
    }

    private User getAuthenticatedAdmin(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getName()) ||
                               "SUPER_ADMIN".equalsIgnoreCase(r.getName()) ||
                               "CLUB_ADMIN".equalsIgnoreCase(r.getName()));

        if (!isAdmin) {
            throw new AccessDeniedException("User does not possess ADMIN privileges.");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getAdminStats(String email) {
        getAuthenticatedAdmin(email);

        long totalStudents = userRepository.countByRoleName("STUDENT");
        long totalFaculty = userRepository.countByRoleName("FACULTY");
        long totalAdmins = userRepository.countByRoleNameIn(List.of("ADMIN", "SUPER_ADMIN", "CLUB_ADMIN"));
        long totalEvents = eventRepository.count();
        long totalAnnouncements = announcementRepository.count();
        long totalClubs = clubRepository.count();
        long totalResources = resourceRepository.count();
        long totalOpportunities = opportunityRepository.count();

        return AdminStatsResponse.builder()
                .totalStudents(totalStudents)
                .totalFaculty(totalFaculty)
                .totalAdmins(totalAdmins)
                .totalEvents(totalEvents)
                .totalAnnouncements(totalAnnouncements)
                .totalClubs(totalClubs)
                .totalAcademicResources(totalResources)
                .totalResources(totalResources)
                .totalOpportunities(totalOpportunities)
                .build();
    }
}
