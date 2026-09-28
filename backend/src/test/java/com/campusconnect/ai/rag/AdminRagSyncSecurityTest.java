package com.campusconnect.ai.rag;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminRagSyncSecurityTest {

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

    @BeforeEach
    void setUp() {
        // Register Student
        RegisterRequest studentRequest = RegisterRequest.builder()
                .firstName("Alice")
                .lastName("Student")
                .email("alice.student.syncsecurity@example.com")
                .password("StudentPass@123")
                .build();
        AuthResponse studentAuth = authService.register(studentRequest);
        studentToken = studentAuth.getToken();

        // Create Faculty user
        String facultyEmail = "prof.faculty.syncsecurity@example.com";
        Role facultyRole = roleRepository.findByName("FACULTY")
                .orElseGet(() -> roleRepository.save(new Role("FACULTY", "Faculty role")));
        User facultyUser = User.builder()
                .firstName("Prof")
                .lastName("Faculty")
                .email(facultyEmail)
                .password(passwordEncoder.encode("FacultyPass@123"))
                .phone("1112223334")
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
        String adminEmail = "admin.user.syncsecurity@example.com";
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "System Admin role")));
        User adminUser = User.builder()
                .firstName("System")
                .lastName("Admin")
                .email(adminEmail)
                .password(passwordEncoder.encode("AdminPass@123"))
                .phone("9998887778")
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
    @DisplayName("ADMIN user should successfully trigger POST /api/admin/rag/sync")
    void testAdminAccessSuccess() throws Exception {
        mockMvc.perform(post("/api/admin/rag/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.totalDocumentsProcessed").isNumber());
    }

    @Test
    @DisplayName("STUDENT user should be denied access (403 Forbidden) to POST /api/admin/rag/sync")
    void testStudentAccessDenied() throws Exception {
        mockMvc.perform(post("/api/admin/rag/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("FACULTY user should be denied access (403 Forbidden) to POST /api/admin/rag/sync")
    void testFacultyAccessDenied() throws Exception {
        mockMvc.perform(post("/api/admin/rag/sync")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Unauthenticated request to POST /api/admin/rag/sync should return 401 Unauthorized")
    void testUnauthenticatedAccessDenied() throws Exception {
        mockMvc.perform(post("/api/admin/rag/sync"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
