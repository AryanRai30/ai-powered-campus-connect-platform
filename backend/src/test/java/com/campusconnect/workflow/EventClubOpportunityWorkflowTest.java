package com.campusconnect.workflow;

import com.campusconnect.dto.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EventClubOpportunityWorkflowTest {

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
    private EventRepository eventRepository;

    @Autowired
    private EventRegistrationRepository registrationRepository;

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

    private String facultyTokenA;
    private String facultyTokenB;
    private String studentToken;
    private User facultyUserA;
    private User facultyUserB;
    private User studentUser;

    @BeforeEach
    void setUp() {
        Role facultyRole = roleRepository.findByName("FACULTY")
                .orElseGet(() -> roleRepository.save(new Role("FACULTY", "Faculty role")));

        // Faculty A
        facultyUserA = User.builder()
                .firstName("Faculty")
                .lastName("One")
                .email("faculty.one.wf@campusconnect.edu")
                .password(passwordEncoder.encode("FacultyPass@123"))
                .isActive(true)
                .build();
        facultyUserA.addRole(facultyRole);
        userRepository.save(facultyUserA);

        AuthResponse authA = authService.login(LoginRequest.builder()
                .email("faculty.one.wf@campusconnect.edu")
                .password("FacultyPass@123")
                .build());
        facultyTokenA = authA.getToken();

        // Faculty B
        facultyUserB = User.builder()
                .firstName("Faculty")
                .lastName("Two")
                .email("faculty.two.wf@campusconnect.edu")
                .password(passwordEncoder.encode("FacultyPass@123"))
                .isActive(true)
                .build();
        facultyUserB.addRole(facultyRole);
        userRepository.save(facultyUserB);

        AuthResponse authB = authService.login(LoginRequest.builder()
                .email("faculty.two.wf@campusconnect.edu")
                .password("FacultyPass@123")
                .build());
        facultyTokenB = authB.getToken();

        // Student
        AuthResponse studentAuth = authService.register(RegisterRequest.builder()
                .firstName("Student")
                .lastName("Workflow")
                .email("student.wf@campusconnect.edu")
                .password("StudentPass@123")
                .build());
        studentToken = studentAuth.getToken();
        studentUser = userRepository.findByEmail("student.wf@campusconnect.edu").orElseThrow();
    }

    @Test
    @DisplayName("EVENT WORKFLOW: Registration, duplicate check, Faculty owner view, and cascade deletion")
    void testEventRegistrationAndDeletionWorkflow() throws Exception {
        // 1. Faculty A creates & publishes Event
        FacultyEventRequest eventReq = FacultyEventRequest.builder()
                .title("Tech Workshop 2026")
                .description("Hands-on AI Workshop")
                .eventDate(java.time.LocalDate.parse("2026-11-15"))
                .eventTime(java.time.LocalTime.parse("10:00:00"))
                .venue("Lab 1")
                .category("Technical")
                .registrationRequired(true)
                .published(true)
                .build();

        String respStr = mockMvc.perform(post("/api/faculty/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        FacultyEventResponse createdEvent = objectMapper.readValue(respStr, FacultyEventResponse.class);
        Long eventId = createdEvent.getId();

        // 2. Student views published events -> registration count is 0
        mockMvc.perform(get("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + eventId + ")].registered").value(false));

        assertThat(registrationRepository.existsByEventIdAndUserId(eventId, studentUser.getId())).isFalse();

        // 3. Student submits registration
        mockMvc.perform(post("/api/events/" + eventId + "/register")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registered").value(true));

        assertThat(registrationRepository.existsByEventIdAndUserId(eventId, studentUser.getId())).isTrue();
        assertThat(registrationRepository.countByEventId(eventId)).isEqualTo(1);

        // 4. Student re-registering should return conflict/error
        mockMvc.perform(post("/api/events/" + eventId + "/register")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().is4xxClientError());

        // 5. Student views My Events -> event is present
        mockMvc.perform(get("/api/events/my-events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + eventId + ")].registered").value(true));

        // 6. Faculty A (owner) can view registrations
        mockMvc.perform(get("/api/faculty/events/" + eventId + "/registrations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("student.wf@campusconnect.edu"));

        // 7. Faculty B (non-owner) receives 403 Forbidden
        mockMvc.perform(get("/api/faculty/events/" + eventId + "/registrations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenB))
                .andExpect(status().isForbidden());

        // 8. Faculty A deletes Event -> child registrations are cascade deleted
        mockMvc.perform(delete("/api/faculty/events/" + eventId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenA))
                .andExpect(status().isNoContent());

        assertThat(eventRepository.findById(eventId)).isEmpty();
        assertThat(registrationRepository.findByEventId(eventId)).isEmpty();
    }

    @Test
    @DisplayName("CLUB WORKFLOW: Membership, duplicate check, Faculty owner view, and cascade deletion")
    void testClubApplicationAndDeletionWorkflow() throws Exception {
        // 1. Faculty A creates & publishes Club
        FacultyClubRequest clubReq = FacultyClubRequest.builder()
                .name("Robotics Club WF")
                .description("Robotics & Automation Community")
                .category("Technical")
                .presidentName("Alex Lead")
                .published(true)
                .build();

        String respStr = mockMvc.perform(post("/api/faculty/clubs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clubReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        FacultyClubResponse createdClub = objectMapper.readValue(respStr, FacultyClubResponse.class);
        Long clubId = createdClub.getId();

        // 2. Student checks club list -> not joined yet
        assertThat(membershipRepository.existsByClubIdAndUserId(clubId, studentUser.getId())).isFalse();

        // 3. Student submits membership application
        mockMvc.perform(post("/api/clubs/" + clubId + "/join")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.joined").value(true));

        assertThat(membershipRepository.existsByClubIdAndUserId(clubId, studentUser.getId())).isTrue();

        // 4. Duplicate membership attempt rejected
        mockMvc.perform(post("/api/clubs/" + clubId + "/join")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().is4xxClientError());

        // 5. Faculty A views club members
        mockMvc.perform(get("/api/faculty/clubs/" + clubId + "/members")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("student.wf@campusconnect.edu"));

        // 6. Faculty B gets 403 Forbidden
        mockMvc.perform(get("/api/faculty/clubs/" + clubId + "/members")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenB))
                .andExpect(status().isForbidden());

        // 7. Faculty A deletes Club -> child memberships are cascade deleted
        mockMvc.perform(delete("/api/faculty/clubs/" + clubId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenA))
                .andExpect(status().isNoContent());

        assertThat(clubRepository.findById(clubId)).isEmpty();
        assertThat(membershipRepository.findByClubId(clubId)).isEmpty();
    }

    @Test
    @DisplayName("OPPORTUNITY WORKFLOW: Internal application, bookmarking, and cascade deletion")
    void testOpportunityWorkflowAndDeletion() throws Exception {
        // 1. Faculty A creates & publishes Opportunity
        FacultyOpportunityRequest oppReq = FacultyOpportunityRequest.builder()
                .title("Software Developer Internship WF")
                .description("Summer 2026 SWE Internship")
                .organization("TechCorp")
                .opportunityType("INTERNSHIP")
                .published(true)
                .build();

        String respStr = mockMvc.perform(post("/api/faculty/opportunities")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oppReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        FacultyOpportunityResponse createdOpp = objectMapper.readValue(respStr, FacultyOpportunityResponse.class);
        Long oppId = createdOpp.getId();

        // 2. Student bookmarks Opportunity
        mockMvc.perform(post("/api/opportunities/" + oppId + "/bookmark")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookmarked").value(true));

        assertThat(bookmarkRepository.existsByOpportunityIdAndUserId(oppId, studentUser.getId())).isTrue();

        // 3. Student submits Internal Application
        mockMvc.perform(post("/api/opportunities/" + oppId + "/apply")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applied").value(true));

        assertThat(applicationRepository.existsByOpportunityIdAndUserId(oppId, studentUser.getId())).isTrue();

        // 4. Duplicate application rejected
        mockMvc.perform(post("/api/opportunities/" + oppId + "/apply")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().is4xxClientError());

        // 5. Faculty A views applications list
        mockMvc.perform(get("/api/faculty/opportunities/" + oppId + "/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("student.wf@campusconnect.edu"));

        // 6. Faculty A deletes Opportunity -> Applications and Bookmarks are deleted
        mockMvc.perform(delete("/api/faculty/opportunities/" + oppId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenA))
                .andExpect(status().isNoContent());

        assertThat(opportunityRepository.findById(oppId)).isEmpty();
        assertThat(applicationRepository.findByOpportunityId(oppId)).isEmpty();
        assertThat(bookmarkRepository.findByOpportunityId(oppId)).isEmpty();
    }
}
