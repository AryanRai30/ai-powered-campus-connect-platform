package com.campusconnect.faculty;

import com.campusconnect.dto.*;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.repository.RoleRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.security.JwtService;
import com.campusconnect.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FacultyContentManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private FacultyResourceService facultyResourceService;

    @Autowired
    private FacultyAnnouncementService facultyAnnouncementService;

    @Autowired
    private FacultyEventService facultyEventService;

    @Autowired
    private FacultyOpportunityService facultyOpportunityService;

    @Autowired
    private FacultyClubService facultyClubService;

    private String facultyAToken;
    private String facultyBToken;
    private String studentToken;
    private String facultyAEmail;
    private String facultyBEmail;
    private String studentEmail;

    @BeforeEach
    void setUp() {
        Role facultyRole = roleRepository.findByName("FACULTY")
                .orElseGet(() -> roleRepository.save(new Role("FACULTY", "Faculty role")));
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("STUDENT", "Student role")));

        // Faculty A
        facultyAEmail = "faculty.a.test@campusconnect.edu";
        User userA = User.builder()
                .firstName("Faculty")
                .lastName("Alpha")
                .email(facultyAEmail)
                .password(passwordEncoder.encode("Pass@12345"))
                .isActive(true)
                .build();
        userA.addRole(facultyRole);
        userRepository.save(userA);
        facultyAToken = jwtService.generateToken(userDetailsService.loadUserByUsername(facultyAEmail));

        // Faculty B
        facultyBEmail = "faculty.b.test@campusconnect.edu";
        User userB = User.builder()
                .firstName("Faculty")
                .lastName("Beta")
                .email(facultyBEmail)
                .password(passwordEncoder.encode("Pass@12345"))
                .isActive(true)
                .build();
        userB.addRole(facultyRole);
        userRepository.save(userB);
        facultyBToken = jwtService.generateToken(userDetailsService.loadUserByUsername(facultyBEmail));

        // Student
        studentEmail = "student.test@campusconnect.edu";
        User student = User.builder()
                .firstName("John")
                .lastName("Student")
                .email(studentEmail)
                .password(passwordEncoder.encode("StudentPass@123"))
                .isActive(true)
                .build();
        student.addRole(studentRole);
        userRepository.save(student);
        studentToken = jwtService.generateToken(userDetailsService.loadUserByUsername(studentEmail));
    }

    // --- 1. RESOURCE TESTS ---

    @Test
    @DisplayName("Faculty A can create, view, edit, publish, unpublish, and delete own resource")
    void facultyResourceLifecycle() {
        FacultyResourceRequest request = FacultyResourceRequest.builder()
                .title("Operating Systems Notes")
                .description("Unit 1 OS Architecture")
                .subject("Operating Systems")
                .category("Computer Science")
                .resourceType("NOTES")
                .published(false)
                .build();

        // Create DRAFT
        FacultyResourceResponse created = facultyResourceService.createResource(request, facultyAEmail);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getPublished()).isFalse();

        // Retrieve list
        var list = facultyResourceService.getFacultyResources(facultyAEmail);
        assertThat(list).hasSize(1);

        // Edit
        request.setTitle("OS Updated Notes");
        FacultyResourceResponse updated = facultyResourceService.updateResource(created.getId(), request, facultyAEmail);
        assertThat(updated.getTitle()).isEqualTo("OS Updated Notes");

        // Publish
        FacultyResourceResponse published = facultyResourceService.publishResource(created.getId(), facultyAEmail);
        assertThat(published.getPublished()).isTrue();

        // Unpublish
        FacultyResourceResponse unpublished = facultyResourceService.unpublishResource(created.getId(), facultyAEmail);
        assertThat(unpublished.getPublished()).isFalse();

        // Delete
        facultyResourceService.deleteResource(created.getId(), facultyAEmail);
        assertThat(facultyResourceService.getFacultyResources(facultyAEmail)).isEmpty();
    }

    @Test
    @DisplayName("Faculty B attempting to edit Faculty A's resource receives 403 Forbidden")
    void facultyBModifyingFacultyAResourceShouldReturn403() throws Exception {
        FacultyResourceResponse created = facultyResourceService.createResource(FacultyResourceRequest.builder()
                .title("Private Faculty A Material")
                .description("Internal test content")
                .subject("Math")
                .published(false)
                .build(), facultyAEmail);

        mockMvc.perform(put("/api/faculty/resources/" + created.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hacked Title\",\"description\":\"hack\",\"subject\":\"Math\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Student accessing faculty resource API receives 403 Forbidden")
    void studentAccessingFacultyResourceApiShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/faculty/resources")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated user calling faculty API receives 401 Unauthorized")
    void unauthenticatedRequestToFacultyApiShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/faculty/resources"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Draft resource is not visible on student public resource API")
    void draftResourceHiddenFromStudents() throws Exception {
        facultyResourceService.createResource(FacultyResourceRequest.builder()
                .title("Draft Secret Material")
                .description("Not for students yet")
                .subject("Physics")
                .published(false)
                .build(), facultyAEmail);

        mockMvc.perform(get("/api/resources")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.title == 'Draft Secret Material')]").doesNotExist());
    }

    @Test
    @DisplayName("Published targeted resource is visible to matching student query")
    void publishedTargetedResourceVisibleToMatchingStudent() throws Exception {
        facultyResourceService.createResource(FacultyResourceRequest.builder()
                .title("CSE 3rd Year AI Notes")
                .description("Artificial Intelligence Overview")
                .subject("Artificial Intelligence")
                .targetDepartment("CSE")
                .targetYear(3)
                .published(true)
                .build(), facultyAEmail);

        mockMvc.perform(get("/api/resources")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.title == 'CSE 3rd Year AI Notes')]").exists());
    }

    // --- 2. ANNOUNCEMENT TESTS ---

    @Test
    @DisplayName("Faculty announcement CRUD and publish workflow works correctly")
    void facultyAnnouncementLifecycle() {
        FacultyAnnouncementRequest request = FacultyAnnouncementRequest.builder()
                .title("Campus Closure Notice")
                .content("Holiday on Friday")
                .category("Notice")
                .published(false)
                .build();

        FacultyAnnouncementResponse created = facultyAnnouncementService.createAnnouncement(request, facultyAEmail);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getPublished()).isFalse();

        FacultyAnnouncementResponse published = facultyAnnouncementService.publishAnnouncement(created.getId(), facultyAEmail);
        assertThat(published.getPublished()).isTrue();

        facultyAnnouncementService.deleteAnnouncement(created.getId(), facultyAEmail);
    }

    @Test
    @DisplayName("Faculty B modifying Faculty A announcement receives 403 Forbidden")
    void facultyBModifyingFacultyAAnnouncementShouldReturn403() throws Exception {
        FacultyAnnouncementResponse created = facultyAnnouncementService.createAnnouncement(FacultyAnnouncementRequest.builder()
                .title("Faculty A Announcement")
                .content("Internal announcement")
                .published(true)
                .build(), facultyAEmail);

        mockMvc.perform(delete("/api/faculty/announcements/" + created.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyBToken))
                .andExpect(status().isForbidden());
    }

    // --- 3. EVENT TESTS ---

    @Test
    @DisplayName("Faculty event lifecycle and registration list retrieval")
    void facultyEventLifecycleAndRegistrations() {
        FacultyEventRequest request = FacultyEventRequest.builder()
                .title("Web Dev Bootcamp")
                .description("Hands-on React & Spring Boot workshop")
                .eventDate(LocalDate.now().plusDays(5))
                .eventTime(LocalTime.of(10, 0))
                .venue("Lab 1")
                .category("Workshop")
                .registrationRequired(true)
                .published(true)
                .build();

        FacultyEventResponse created = facultyEventService.createEvent(request, facultyAEmail);
        assertThat(created.getId()).isNotNull();

        var registrations = facultyEventService.getEventRegistrations(created.getId(), facultyAEmail);
        assertThat(registrations).isEmpty();
    }

    @Test
    @DisplayName("Faculty B modifying Faculty A event receives 403 Forbidden")
    void facultyBModifyingFacultyAEventShouldReturn403() throws Exception {
        FacultyEventResponse created = facultyEventService.createEvent(FacultyEventRequest.builder()
                .title("Faculty A Event")
                .description("Exclusive event")
                .eventDate(LocalDate.now().plusDays(3))
                .venue("Auditorium")
                .published(true)
                .build(), facultyAEmail);

        mockMvc.perform(post("/api/faculty/events/" + created.getId() + "/unpublish")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyBToken))
                .andExpect(status().isForbidden());
    }

    // --- 4. OPPORTUNITY TESTS ---

    @Test
    @DisplayName("Faculty opportunity creation and publish workflow")
    void facultyOpportunityLifecycle() {
        FacultyOpportunityRequest request = FacultyOpportunityRequest.builder()
                .title("Backend Intern")
                .description("Spring Boot developer opportunity")
                .organization("TechCorp")
                .opportunityType("INTERNSHIP")
                .location("Remote")
                .published(true)
                .build();

        FacultyOpportunityResponse created = facultyOpportunityService.createOpportunity(request, facultyAEmail);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getPublished()).isTrue();

        FacultyOpportunityResponse unpublished = facultyOpportunityService.unpublishOpportunity(created.getId(), facultyAEmail);
        assertThat(unpublished.getPublished()).isFalse();
    }

    @Test
    @DisplayName("Faculty B modifying Faculty A opportunity receives 403 Forbidden")
    void facultyBModifyingFacultyAOpportunityShouldReturn403() throws Exception {
        FacultyOpportunityResponse created = facultyOpportunityService.createOpportunity(FacultyOpportunityRequest.builder()
                .title("Faculty A Job")
                .description("Developer role")
                .organization("Acme")
                .published(true)
                .build(), facultyAEmail);

        mockMvc.perform(put("/api/faculty/opportunities/" + created.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Altered\",\"description\":\"desc\",\"organization\":\"Acme\"}"))
                .andExpect(status().isForbidden());
    }

    // --- 5. CLUB TESTS ---

    @Test
    @DisplayName("Faculty club management lifecycle")
    void facultyClubLifecycle() {
        FacultyClubRequest request = FacultyClubRequest.builder()
                .name("Campus AI Society " + System.currentTimeMillis())
                .description("Exploring Artificial Intelligence & Machine Learning")
                .category("Technical")
                .published(true)
                .build();

        FacultyClubResponse created = facultyClubService.createClub(request, facultyAEmail);
        assertThat(created.getId()).isNotNull();

        var members = facultyClubService.getClubMembers(created.getId(), facultyAEmail);
        assertThat(members).isEmpty();
    }

    @Test
    @DisplayName("Faculty B modifying Faculty A club receives 403 Forbidden")
    void facultyBModifyingFacultyAClubShouldReturn403() throws Exception {
        FacultyClubResponse created = facultyClubService.createClub(FacultyClubRequest.builder()
                .name("Faculty A Club " + System.currentTimeMillis())
                .description("Faculty A supervised club")
                .published(true)
                .build(), facultyAEmail);

        mockMvc.perform(delete("/api/faculty/clubs/" + created.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyBToken))
                .andExpect(status().isForbidden());
    }
}
