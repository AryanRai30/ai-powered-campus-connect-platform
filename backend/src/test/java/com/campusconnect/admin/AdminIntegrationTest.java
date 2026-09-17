package com.campusconnect.admin;

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
public class AdminIntegrationTest {

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
    private String adminToken;
    private String adminEmail;

    @BeforeEach
    void setUp() {
        // Register Student
        RegisterRequest studentRequest = RegisterRequest.builder()
                .firstName("Alice")
                .lastName("Student")
                .email("alice.student.admintest@example.com")
                .password("StudentPass@123")
                .build();
        AuthResponse studentAuth = authService.register(studentRequest);
        studentToken = studentAuth.getToken();

        // Create Faculty user
        String facultyEmail = "prof.faculty.admintest@example.com";
        Role facultyRole = roleRepository.findByName("FACULTY")
                .orElseGet(() -> roleRepository.save(new Role("FACULTY", "Faculty role")));
        User facultyUser = User.builder()
                .firstName("Prof")
                .lastName("Faculty")
                .email(facultyEmail)
                .password(passwordEncoder.encode("FacultyPass@123"))
                .phone("1112223333")
                .isActive(true)
                .build();
        facultyUser.addRole(facultyRole);
        userRepository.save(facultyUser);

        LoginRequest facultyLogin = LoginRequest.builder()
                .email(facultyEmail)
                .password("FacultyPass@123")
                .build();
        AuthResponse facultyAuth = authService.login(facultyLogin);
        facultyToken = facultyAuth.getToken();

        // Create Admin user
        adminEmail = "admin.user.admintest@example.com";
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "System Admin role")));
        User adminUser = User.builder()
                .firstName("System")
                .lastName("Admin")
                .email(adminEmail)
                .password(passwordEncoder.encode("AdminPass@123"))
                .phone("9998887777")
                .isActive(true)
                .build();
        adminUser.addRole(adminRole);
        userRepository.save(adminUser);

        LoginRequest adminLogin = LoginRequest.builder()
                .email(adminEmail)
                .password("AdminPass@123")
                .build();
        AuthResponse adminAuth = authService.login(adminLogin);
        adminToken = adminAuth.getToken();
    }

    @Test
    @DisplayName("ADMIN should successfully access GET /api/admin/stats and receive real database counts")
    void adminShouldAccessAdminStats() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").isNumber())
                .andExpect(jsonPath("$.totalFaculty").isNumber())
                .andExpect(jsonPath("$.totalAdmins").isNumber())
                .andExpect(jsonPath("$.totalEvents").isNumber())
                .andExpect(jsonPath("$.totalAnnouncements").isNumber())
                .andExpect(jsonPath("$.totalClubs").isNumber())
                .andExpect(jsonPath("$.totalAcademicResources").isNumber())
                .andExpect(jsonPath("$.totalOpportunities").isNumber());
    }

    @Test
    @DisplayName("FACULTY accessing GET /api/admin/stats should receive 403 Forbidden")
    void facultyAccessingAdminStatsShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("STUDENT accessing GET /api/admin/stats should receive 403 Forbidden")
    void studentAccessingAdminStatsShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Unauthenticated request to GET /api/admin/stats should receive 401 Unauthorized")
    void unauthenticatedAccessingAdminStatsShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
