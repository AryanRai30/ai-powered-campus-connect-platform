package com.campusconnect.faculty;

import com.campusconnect.dto.AuthResponse;
import com.campusconnect.dto.LoginRequest;
import com.campusconnect.dto.RegisterRequest;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.repository.RoleRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FacultyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String studentToken;
    private String facultyToken;
    private String facultyEmail;

    @BeforeEach
    void setUp() {
        // Register a Student
        RegisterRequest studentRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Student")
                .email("john.student.facultytest@example.com")
                .password("StudentPass@123")
                .build();
        AuthResponse studentAuth = authService.register(studentRequest);
        studentToken = studentAuth.getToken();

        // Create a Faculty user directly (authorized creation mechanism)
        facultyEmail = "prof.smith.test@example.com";
        Role facultyRole = roleRepository.findByName("FACULTY")
                .orElseGet(() -> roleRepository.save(new Role("FACULTY", "Faculty role")));

        User facultyUser = User.builder()
                .firstName("Professor")
                .lastName("Smith")
                .email(facultyEmail)
                .password(passwordEncoder.encode("FacultyPass@123"))
                .phone("1234567890")
                .isActive(true)
                .build();
        facultyUser.addRole(facultyRole);
        userRepository.save(facultyUser);

        // Authenticate Faculty user via login
        LoginRequest facultyLogin = LoginRequest.builder()
                .email(facultyEmail)
                .password("FacultyPass@123")
                .build();
        AuthResponse facultyAuth = authService.login(facultyLogin);
        facultyToken = facultyAuth.getToken();
    }

    @Test
    @DisplayName("Faculty login should succeed and return token with FACULTY role")
    void facultyLoginShouldSucceedWithFacultyRole() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(facultyEmail)
                .password("FacultyPass@123")
                .build();

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getEmail()).isEqualTo(facultyEmail);
        assertThat(response.getRoles()).contains("FACULTY");
    }

    @Test
    @DisplayName("Faculty should successfully access GET /api/faculty/dashboard")
    void facultyShouldAccessFacultyDashboard() throws Exception {
        mockMvc.perform(get("/api/faculty/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(facultyEmail))
                .andExpect(jsonPath("$.firstName").value("Professor"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.role").value("FACULTY"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Student accessing GET /api/faculty/dashboard should receive 403 Forbidden")
    void studentAccessingFacultyDashboardShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/faculty/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Unauthenticated request to GET /api/faculty/dashboard should receive 401 Unauthorized")
    void unauthenticatedRequestToFacultyDashboardShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/faculty/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
