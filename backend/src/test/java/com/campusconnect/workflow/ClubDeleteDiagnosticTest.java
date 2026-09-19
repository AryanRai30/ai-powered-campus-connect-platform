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
public class ClubDeleteDiagnosticTest {

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
    private ClubRepository clubRepository;

    @Autowired
    private ClubMembershipRepository membershipRepository;

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
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("STUDENT", "Student role")));

        // Faculty A (Owner)
        facultyUserA = User.builder()
                .firstName("Faculty")
                .lastName("Alpha")
                .email("faculty.alpha.diag@campusconnect.edu")
                .password(passwordEncoder.encode("FacultyPass@123"))
                .isActive(true)
                .build();
        facultyUserA.addRole(facultyRole);
        userRepository.save(facultyUserA);

        AuthResponse authA = authService.login(LoginRequest.builder()
                .email("faculty.alpha.diag@campusconnect.edu")
                .password("FacultyPass@123")
                .build());
        facultyTokenA = authA.getToken();

        // Faculty B (Non-owner)
        facultyUserB = User.builder()
                .firstName("Faculty")
                .lastName("Beta")
                .email("faculty.beta.diag@campusconnect.edu")
                .password(passwordEncoder.encode("FacultyPass@123"))
                .isActive(true)
                .build();
        facultyUserB.addRole(facultyRole);
        userRepository.save(facultyUserB);

        AuthResponse authB = authService.login(LoginRequest.builder()
                .email("faculty.beta.diag@campusconnect.edu")
                .password("FacultyPass@123")
                .build());
        facultyTokenB = authB.getToken();

        // Student
        studentUser = User.builder()
                .firstName("Student")
                .lastName("Diag")
                .email("student.diag@campusconnect.edu")
                .password(passwordEncoder.encode("StudentPass@123"))
                .isActive(true)
                .build();
        studentUser.addRole(studentRole);
        userRepository.save(studentUser);

        AuthResponse authS = authService.login(LoginRequest.builder()
                .email("student.diag@campusconnect.edu")
                .password("StudentPass@123")
                .build());
        studentToken = authS.getToken();
    }

    @Test
    @DisplayName("DIAGNOSTIC 1: Preflight OPTIONS request on /api/faculty/clubs/{id}")
    void testPreflightOptionsRequest() throws Exception {
        mockMvc.perform(options("/api/faculty/clubs/1")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5174")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DIAGNOSTIC 2: Unauthenticated DELETE request triggers 401 Unauthorized")
    void testUnauthenticatedDelete() throws Exception {
        mockMvc.perform(delete("/api/faculty/clubs/999"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"));
    }

    @Test
    @DisplayName("DIAGNOSTIC 3: Student DELETE request triggers 403 Forbidden")
    void testStudentDelete() throws Exception {
        mockMvc.perform(delete("/api/faculty/clubs/999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DIAGNOSTIC 4: Faculty B (Non-owner) DELETE request triggers 403 Forbidden")
    void testNonOwnerFacultyDelete() throws Exception {
        // Faculty A creates club
        FacultyClubRequest req = FacultyClubRequest.builder()
                .name("Diag Club Alpha")
                .description("Diagnostic Testing Club")
                .category("Technical")
                .published(true)
                .build();

        Club club = Club.builder()
                .name(req.getName())
                .description(req.getDescription())
                .category(req.getCategory())
                .published(true)
                .active(true)
                .createdBy(facultyUserA)
                .updatedBy(facultyUserA)
                .build();
        club = clubRepository.save(club);

        // Faculty B tries to delete Faculty A's club -> 403 Forbidden
        mockMvc.perform(delete("/api/faculty/clubs/" + club.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenB))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DIAGNOSTIC 5: Faculty A (Owner) DELETE request succeeds (204 No Content) and cascades memberships")
    void testOwnerFacultyDeleteSuccess() throws Exception {
        // 1. Create Club owned by Faculty A
        Club club = Club.builder()
                .name("Diag Club Success")
                .description("Diagnostic Testing Club Success")
                .category("Cultural")
                .published(true)
                .active(true)
                .createdBy(facultyUserA)
                .updatedBy(facultyUserA)
                .build();
        club = clubRepository.save(club);

        // 2. Student joins
        ClubMembership membership = ClubMembership.builder()
                .club(club)
                .user(studentUser)
                .build();
        membershipRepository.save(membership);
        assertThat(membershipRepository.findByClubId(club.getId())).hasSize(1);

        // 3. Faculty A deletes own club
        mockMvc.perform(delete("/api/faculty/clubs/" + club.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenA))
                .andExpect(status().isNoContent());

        // 4. Verify DB
        assertThat(clubRepository.findById(club.getId())).isEmpty();
        assertThat(membershipRepository.findByClubId(club.getId())).isEmpty();
    }
}
