package com.campusconnect.faculty;

import com.campusconnect.dto.*;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.StudentProfile;
import com.campusconnect.entity.User;
import com.campusconnect.repository.RoleRepository;
import com.campusconnect.repository.StudentProfileRepository;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FacultyPhase83IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StudentProfileRepository profileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthService authService;

    @Autowired
    private StudentProfileService studentProfileService;

    @Autowired
    private FacultyResourceService facultyResourceService;

    @Autowired
    private FacultyEventService facultyEventService;

    @Autowired
    private FacultyOpportunityService facultyOpportunityService;

    @Autowired
    private FacultyClubService facultyClubService;

    @Autowired
    private EventService studentEventService;

    @Autowired
    private ClubService studentClubService;

    @Autowired
    private OpportunityService studentOpportunityService;

    private String facultyAToken;
    private String facultyBToken;
    private String student1Token;
    private String student2Token;

    private String facultyAEmail;
    private String facultyBEmail;
    private String student1Email;
    private String student2Email;

    @BeforeEach
    void setUp() {
        Role facultyRole = roleRepository.findByName("FACULTY")
                .orElseGet(() -> roleRepository.save(new Role("FACULTY", "Faculty role")));
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("STUDENT", "Student role")));

        // Faculty A
        facultyAEmail = "faculty.alpha.83@campusconnect.edu";
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
        facultyBEmail = "faculty.beta.83@campusconnect.edu";
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

        // Student 1 (CSE, B.Tech CSE, Year 3, Sem 5)
        student1Email = "student1.cse@campusconnect.edu";
        User s1 = User.builder()
                .firstName("Alice")
                .lastName("CSE")
                .email(student1Email)
                .password(passwordEncoder.encode("Pass@12345"))
                .isActive(true)
                .build();
        s1.addRole(studentRole);
        userRepository.save(s1);
        profileRepository.save(StudentProfile.builder()
                .user(s1)
                .studentId("STU-CSE-001")
                .course("B.Tech CSE")
                .department("CSE")
                .year("3")
                .semester("5")
                .build());
        student1Token = jwtService.generateToken(userDetailsService.loadUserByUsername(student1Email));

        // Student 2 (ECE, B.Tech ECE, Year 2, Sem 3)
        student2Email = "student2.ece@campusconnect.edu";
        User s2 = User.builder()
                .firstName("Bob")
                .lastName("ECE")
                .email(student2Email)
                .password(passwordEncoder.encode("Pass@12345"))
                .isActive(true)
                .build();
        s2.addRole(studentRole);
        userRepository.save(s2);
        profileRepository.save(StudentProfile.builder()
                .user(s2)
                .studentId("STU-ECE-002")
                .course("B.Tech ECE")
                .department("ECE")
                .year("2")
                .semester("3")
                .build());
        student2Token = jwtService.generateToken(userDetailsService.loadUserByUsername(student2Email));
    }

    // --- 1. FACULTY STUDENTS OVERVIEW TESTS ---

    @Test
    @DisplayName("Faculty can retrieve student directory")
    void facultyCanRetrieveStudents() throws Exception {
        mockMvc.perform(get("/api/faculty/students")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.studentId == 'STU-CSE-001')].firstName").value("Alice"))
                .andExpect(jsonPath("$[?(@.studentId == 'STU-ECE-002')].firstName").value("Bob"));
    }

    @Test
    @DisplayName("Faculty student endpoint supports filtering by department")
    void facultyStudentsFilteringByDepartment() throws Exception {
        mockMvc.perform(get("/api/faculty/students?department=CSE")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].studentId").value("STU-CSE-001"));
    }

    @Test
    @DisplayName("Student accessing faculty student directory receives 403 Forbidden")
    void studentAccessingFacultyStudentsShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/faculty/students")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + student1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated user accessing faculty student directory receives 401 Unauthorized")
    void unauthenticatedAccessingFacultyStudentsShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/faculty/students"))
                .andExpect(status().isUnauthorized());
    }

    // --- 2. FACULTY OPPORTUNITY APPLICATION MONITORING TESTS ---

    @Test
    @DisplayName("Faculty can view applications for own opportunity; Faculty B receives 403 Forbidden")
    void facultyOpportunityApplicationMonitoringAndSecurity() throws Exception {
        // Faculty A creates opportunity
        FacultyOpportunityResponse opp = facultyOpportunityService.createOpportunity(FacultyOpportunityRequest.builder()
                .title("AI Research Assistant")
                .description("Machine Learning Project")
                .organization("AI Lab")
                .published(true)
                .build(), facultyAEmail);

        // Student 1 applies
        studentOpportunityService.applyForOpportunity(opp.getId(), student1Email);

        // Faculty A views applications -> 200 OK
        mockMvc.perform(get("/api/faculty/opportunities/" + opp.getId() + "/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].studentId").value("STU-CSE-001"))
                .andExpect(jsonPath("$[0].applicationStatus").value("APPLIED"));

        // Faculty B views Faculty A's applications -> 403 Forbidden
        mockMvc.perform(get("/api/faculty/opportunities/" + opp.getId() + "/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyBToken))
                .andExpect(status().isForbidden());

        // Student views applications -> 403 Forbidden
        mockMvc.perform(get("/api/faculty/opportunities/" + opp.getId() + "/applications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + student1Token))
                .andExpect(status().isForbidden());
    }

    // --- 3. TARGETING VERIFICATION TESTS ---

    @Test
    @DisplayName("Targeted published resource visible to matching student but hidden from non-matching student")
    void targetingVerification() throws Exception {
        // Targeted to CSE 3rd Year
        facultyResourceService.createResource(FacultyResourceRequest.builder()
                .title("Advanced Algorithms Unit 2")
                .description("Graph Algorithms")
                .subject("Algorithms")
                .targetDepartment("CSE")
                .targetYear(3)
                .published(true)
                .build(), facultyAEmail);

        // General Resource
        facultyResourceService.createResource(FacultyResourceRequest.builder()
                .title("Campus Library Guide")
                .description("General Info")
                .subject("General")
                .published(true)
                .build(), facultyAEmail);

        // Draft Resource
        facultyResourceService.createResource(FacultyResourceRequest.builder()
                .title("Draft Exam Paper")
                .description("Confidential")
                .subject("Exam")
                .published(false)
                .build(), facultyAEmail);

        // Student 1 (CSE, Year 3) sees targeted and general resource, NOT draft
        mockMvc.perform(get("/api/resources")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.title == 'Advanced Algorithms Unit 2')]").exists())
                .andExpect(jsonPath("$[?(@.title == 'Campus Library Guide')]").exists())
                .andExpect(jsonPath("$[?(@.title == 'Draft Exam Paper')]").doesNotExist());

        // Student 2 (ECE, Year 2) sees general resource, NOT targeted to CSE, NOT draft
        mockMvc.perform(get("/api/resources")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + student2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.title == 'Campus Library Guide')]").exists())
                .andExpect(jsonPath("$[?(@.title == 'Advanced Algorithms Unit 2')]").doesNotExist())
                .andExpect(jsonPath("$[?(@.title == 'Draft Exam Paper')]").doesNotExist());
    }

    // --- 4. REGRESSION TESTS FOR PHASES 1–7 ---

    @Test
    @DisplayName("Regression: Student authentication, profile, registration, club joining, bookmarking, and application tracking still work seamlessly")
    void regressionTestingPhases1To7() {
        // Auth login
        AuthResponse loginRes = authService.login(LoginRequest.builder()
                .email(student1Email)
                .password("Pass@12345")
                .build());
        assertThat(loginRes.getToken()).isNotBlank();

        // Student profile
        StudentProfileResponse profile = studentProfileService.getCurrentUserProfile(student1Email);
        assertThat(profile.getStudentId()).isEqualTo("STU-CSE-001");

        // Event registration
        FacultyEventResponse event = facultyEventService.createEvent(FacultyEventRequest.builder()
                .title("Phase 8.3 Workshop")
                .description("Regression Test Event")
                .eventDate(LocalDate.now().plusDays(10))
                .venue("Hall B")
                .registrationRequired(true)
                .published(true)
                .build(), facultyAEmail);

        RegistrationStatusResponse regStatus = studentEventService.registerForEvent(event.getId(), student1Email);
        assertThat(regStatus.isRegistered()).isTrue();

        // Club joining
        FacultyClubResponse club = facultyClubService.createClub(FacultyClubRequest.builder()
                .name("Regression Robotics Club " + System.currentTimeMillis())
                .description("Robotics")
                .published(true)
                .build(), facultyAEmail);

        ClubMembershipStatusResponse clubStatus = studentClubService.joinClub(club.getId(), student1Email);
        assertThat(clubStatus.isJoined()).isTrue();

        // Opportunity bookmarking & applying
        FacultyOpportunityResponse opp = facultyOpportunityService.createOpportunity(FacultyOpportunityRequest.builder()
                .title("DevOps Internship")
                .description("CI/CD pipeline developer")
                .organization("CloudInc")
                .published(true)
                .build(), facultyAEmail);

        OpportunityBookmarkStatusResponse bmStatus = studentOpportunityService.bookmarkOpportunity(opp.getId(), student1Email);
        assertThat(bmStatus.isBookmarked()).isTrue();

        OpportunityApplicationStatusResponse appStatus = studentOpportunityService.applyForOpportunity(opp.getId(), student1Email);
        assertThat(appStatus.isApplied()).isTrue();
    }
}
