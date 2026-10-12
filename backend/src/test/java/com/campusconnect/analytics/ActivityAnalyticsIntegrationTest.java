package com.campusconnect.analytics;

import com.campusconnect.dto.AuthResponse;
import com.campusconnect.dto.RegisterRequest;
import com.campusconnect.entity.*;
import com.campusconnect.repository.*;
import com.campusconnect.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ActivityAnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StudentProfileRepository profileRepository;

    @Autowired
    private AcademicResourceRepository resourceRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubMembershipRepository membershipRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private OpportunityApplicationRepository applicationRepository;

    @Autowired
    private OpportunityBookmarkRepository bookmarkRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String studentToken;
    private String facultyToken;
    private String adminToken;

    private User studentUser;
    private User facultyUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("STUDENT", "Student Role")));
        Role facultyRole = roleRepository.findByName("FACULTY")
                .orElseGet(() -> roleRepository.save(new Role("FACULTY", "Faculty Role")));
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Admin Role")));

        // Create Student User
        RegisterRequest studentReq = RegisterRequest.builder()
                .firstName("Analytics")
                .lastName("Student")
                .email("analytics.student@campus.edu")
                .password("Password123!")
                .build();
        AuthResponse studentAuth = authService.register(studentReq);
        studentToken = studentAuth.getToken();
        studentUser = userRepository.findByEmail("analytics.student@campus.edu").orElseThrow();

        StudentProfile sp = profileRepository.findByUserId(studentUser.getId()).orElse(null);
        if (sp != null) {
            sp.setDepartment("Computer Science");
            sp.setCourse("B.Tech CSE");
            sp.setYear("3");
            sp.setSemester("6");
            profileRepository.save(sp);
        }

        // Create Faculty User
        facultyUser = userRepository.findByEmail("analytics.faculty@campus.edu").orElse(null);
        if (facultyUser == null) {
            facultyUser = User.builder()
                    .firstName("Prof")
                    .lastName("Analytics")
                    .email("analytics.faculty@campus.edu")
                    .password(passwordEncoder.encode("Password123!"))
                    .roles(Set.of(facultyRole))
                    .isActive(true)
                    .build();
            facultyUser = userRepository.save(facultyUser);
        }
        AuthResponse facultyAuth = authService.login(new com.campusconnect.dto.LoginRequest("analytics.faculty@campus.edu", "Password123!"));
        facultyToken = facultyAuth.getToken();

        // Create Admin User
        adminUser = userRepository.findByEmail("analytics.admin@campus.edu").orElse(null);
        if (adminUser == null) {
            adminUser = User.builder()
                    .firstName("System")
                    .lastName("Admin")
                    .email("analytics.admin@campus.edu")
                    .password(passwordEncoder.encode("Password123!"))
                    .roles(Set.of(adminRole))
                    .isActive(true)
                    .build();
            adminUser = userRepository.save(adminUser);
        }
        AuthResponse adminAuth = authService.login(new com.campusconnect.dto.LoginRequest("analytics.admin@campus.edu", "Password123!"));
        adminToken = adminAuth.getToken();
    }

    @Test
    @DisplayName("Student Activity Analytics calculates real database metrics correctly")
    void testStudentActivityAnalytics() throws Exception {
        // Setup real database content & student interactions
        Event event = eventRepository.save(Event.builder()
                .title("Data Analytics Symposium")
                .description("Explore big data & ML tools.")
                .eventDate(LocalDate.now().plusDays(5))
                .eventTime(LocalTime.of(10, 0))
                .venue("Main Auditorium")
                .category("TECHNICAL")
                .createdBy(facultyUser)
                .published(true)
                .active(true)
                .build());

        registrationRepository.save(EventRegistration.builder()
                .event(event)
                .user(studentUser)
                .build());

        Club club = clubRepository.save(Club.builder()
                .name("Analytics Club")
                .description("Student data science club")
                .category("Technical")
                .createdBy(facultyUser)
                .published(true)
                .active(true)
                .build());

        membershipRepository.save(ClubMembership.builder()
                .club(club)
                .user(studentUser)
                .build());

        Opportunity opportunity = opportunityRepository.save(Opportunity.builder()
                .title("Data Analyst Intern")
                .description("Internship in analytics team")
                .organization("TechCorp")
                .opportunityType("INTERNSHIP")
                .createdBy(facultyUser)
                .published(true)
                .active(true)
                .build());

        applicationRepository.save(OpportunityApplication.builder()
                .opportunity(opportunity)
                .user(studentUser)
                .status("APPLIED")
                .build());

        bookmarkRepository.save(OpportunityBookmark.builder()
                .opportunity(opportunity)
                .user(studentUser)
                .build());

        mockMvc.perform(get("/api/analytics/student")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEventRegistrations", is(1)))
                .andExpect(jsonPath("$.totalClubMemberships", is(1)))
                .andExpect(jsonPath("$.totalOpportunityApplications", is(1)))
                .andExpect(jsonPath("$.totalOpportunityBookmarks", is(1)))
                .andExpect(jsonPath("$.recentActivities", hasSize(4)))
                .andExpect(jsonPath("$.registeredEvents[0].title", is("Data Analytics Symposium")))
                .andExpect(jsonPath("$.joinedClubs[0].name", is("Analytics Club")))
                .andExpect(jsonPath("$.applications[0].title", is("Data Analyst Intern")))
                .andExpect(jsonPath("$.bookmarks[0].title", is("Data Analyst Intern")));
    }

    @Test
    @DisplayName("Faculty Activity Analytics returns correct faculty content & engagement stats")
    void testFacultyActivityAnalytics() throws Exception {
        // Faculty creates resource & event
        resourceRepository.save(AcademicResource.builder()
                .title("DBMS Lecture Notes")
                .description("SQL & Normalization notes")
                .subject("Database Systems")
                .createdBy(facultyUser)
                .published(true)
                .active(true)
                .build());

        Event event = eventRepository.save(Event.builder()
                .title("Faculty Tech Talk")
                .description("Seminar on Cloud Systems")
                .eventDate(LocalDate.now().plusDays(10))
                .venue("Seminar Hall B")
                .createdBy(facultyUser)
                .published(true)
                .active(true)
                .build());

        registrationRepository.save(EventRegistration.builder()
                .event(event)
                .user(studentUser)
                .build());

        mockMvc.perform(get("/api/analytics/faculty")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishedResourcesCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.eventsCreatedCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalEventRegistrationsReceived", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.eventEngagementBreakdown[0].title", is("Faculty Tech Talk")))
                .andExpect(jsonPath("$.eventEngagementBreakdown[0].totalEngagement", is(1)));
    }

    @Test
    @DisplayName("Admin Platform Analytics returns overall system-wide metrics")
    void testAdminPlatformAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalFaculty", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalAdmins", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.publishedResourcesCount", notNullValue()))
                .andExpect(jsonPath("$.eventsCount", notNullValue()))
                .andExpect(jsonPath("$.clubsCount", notNullValue()))
                .andExpect(jsonPath("$.opportunitiesCount", notNullValue()));
    }

    @Test
    @DisplayName("Role authorization strictly enforces endpoint access boundaries")
    void testRoleAuthorizationForAnalytics() throws Exception {
        // Student accessing Faculty analytics -> 403 Forbidden
        mockMvc.perform(get("/api/analytics/faculty")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        // Student accessing Admin analytics -> 403 Forbidden
        mockMvc.perform(get("/api/analytics/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        // Unauthenticated access -> 401 Unauthorized
        mockMvc.perform(get("/api/analytics/student"))
                .andExpect(status().isUnauthorized());
    }
}
