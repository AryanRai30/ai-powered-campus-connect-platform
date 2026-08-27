package com.campusconnect.config;

import com.campusconnect.entity.Role;
import com.campusconnect.repository.AcademicResourceRepository;
import com.campusconnect.repository.AnnouncementRepository;
import com.campusconnect.repository.ClubMembershipRepository;
import com.campusconnect.repository.ClubRepository;
import com.campusconnect.repository.EventRegistrationRepository;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.repository.OpportunityApplicationRepository;
import com.campusconnect.repository.OpportunityBookmarkRepository;
import com.campusconnect.repository.OpportunityRepository;
import com.campusconnect.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initializes essential system seed roles (STUDENT, FACULTY, CLUB_ADMIN, SUPER_ADMIN)
 * and safely purges legacy demo/sample campus content to enforce the Real Data Only principle.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final RoleRepository roleRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final AnnouncementRepository announcementRepository;
    private final ClubRepository clubRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    private final AcademicResourceRepository resourceRepository;
    private final OpportunityRepository opportunityRepository;
    private final OpportunityBookmarkRepository opportunityBookmarkRepository;
    private final OpportunityApplicationRepository opportunityApplicationRepository;

    public DataInitializer(
            RoleRepository roleRepository,
            EventRepository eventRepository,
            EventRegistrationRepository eventRegistrationRepository,
            AnnouncementRepository announcementRepository,
            ClubRepository clubRepository,
            ClubMembershipRepository clubMembershipRepository,
            AcademicResourceRepository resourceRepository,
            OpportunityRepository opportunityRepository,
            OpportunityBookmarkRepository opportunityBookmarkRepository,
            OpportunityApplicationRepository opportunityApplicationRepository
    ) {
        this.roleRepository = roleRepository;
        this.eventRepository = eventRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.announcementRepository = announcementRepository;
        this.clubRepository = clubRepository;
        this.clubMembershipRepository = clubMembershipRepository;
        this.resourceRepository = resourceRepository;
        this.opportunityRepository = opportunityRepository;
        this.opportunityBookmarkRepository = opportunityBookmarkRepository;
        this.opportunityApplicationRepository = opportunityApplicationRepository;
    }

    @Override
    public void run(String... args) {
        seedRoles();
        cleanDemoCampusContent();
    }

    private void seedRoles() {
        try {
            List<Role> defaultRoles = List.of(
                new Role("STUDENT", "Student role with access to academic and campus features"),
                new Role("FACULTY", "Faculty role for managing courses and academic resources"),
                new Role("CLUB_ADMIN", "Club administrator role for event and activity management"),
                new Role("SUPER_ADMIN", "Super administrator role with full system privileges")
            );

            for (Role role : defaultRoles) {
                if (!roleRepository.existsByName(role.getName())) {
                    roleRepository.save(role);
                    logger.info("Initialized default role: {}", role.getName());
                }
            }
        } catch (Exception e) {
            logger.warn("DataInitializer skipped role seeding: {}", e.getMessage());
        }
    }

    private void cleanDemoCampusContent() {
        try {
            logger.info("Purging legacy demo campus content to enforce Real Data Only architecture...");
            eventRegistrationRepository.deleteAllInBatch();
            clubMembershipRepository.deleteAllInBatch();
            opportunityBookmarkRepository.deleteAllInBatch();
            opportunityApplicationRepository.deleteAllInBatch();
            eventRepository.deleteAllInBatch();
            announcementRepository.deleteAllInBatch();
            clubRepository.deleteAllInBatch();
            resourceRepository.deleteAllInBatch();
            opportunityRepository.deleteAllInBatch();
            logger.info("Demo campus content successfully purged. User accounts, roles, and profiles preserved.");
        } catch (Exception e) {
            logger.warn("DataInitializer skipped demo content cleanup: {}", e.getMessage());
        }
    }
}
