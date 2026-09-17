package com.campusconnect.admin;

import com.campusconnect.dto.AuthResponse;
import com.campusconnect.dto.CreateFacultyRequest;
import com.campusconnect.dto.LoginRequest;
import com.campusconnect.dto.RegisterRequest;
import com.campusconnect.dto.UpdateFacultyRequest;
import com.campusconnect.dto.UpdateFacultyStatusRequest;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.repository.RoleRepository;
import com.campusconnect.repository.UserRepository;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminFacultyIntegrationTest {

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
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String facultyToken;
    private String studentToken;
    private User testFacultyUser;

    @BeforeEach
    void setUp() {
        // Create Admin user
        String adminEmail = "admin.facultytest@example.com";
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Admin role")));
        User adminUser = User.builder()
                .firstName("Admin")
                .lastName("User")
                .email(adminEmail)
                .password(passwordEncoder.encode("AdminPass@123"))
                .isActive(true)
                .build();
        adminUser.addRole(adminRole);
        userRepository.save(adminUser);

        AuthResponse adminAuth = authService.login(LoginRequest.builder()
                .email(adminEmail)
                .password("AdminPass@123")
                .build());
        adminToken = adminAuth.getToken();

        // Create initial Faculty user
        String facultyEmail = "faculty.initial@example.com";
        Role facultyRole = roleRepository.findByName("FACULTY")
                .orElseGet(() -> roleRepository.save(new Role("FACULTY", "Faculty role")));
        testFacultyUser = User.builder()
                .firstName("Initial")
                .lastName("Faculty")
                .email(facultyEmail)
                .password(passwordEncoder.encode("FacultyPass@123"))
                .phone("1234567890")
                .isActive(true)
                .build();
        testFacultyUser.addRole(facultyRole);
        userRepository.save(testFacultyUser);

        AuthResponse facultyAuth = authService.login(LoginRequest.builder()
                .email(facultyEmail)
                .password("FacultyPass@123")
                .build());
        facultyToken = facultyAuth.getToken();

        // Register Student
        AuthResponse studentAuth = authService.register(RegisterRequest.builder()
                .firstName("Student")
                .lastName("User")
                .email("student.facultytest@example.com")
                .password("StudentPass@123")
                .build());
        studentToken = studentAuth.getToken();
    }

    @Test
    @DisplayName("ADMIN should list all faculty users")
    void adminShouldListFaculty() throws Exception {
        mockMvc.perform(get("/api/admin/faculty")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(notNullValue())))
                .andExpect(jsonPath("$[0].email").value("faculty.initial@example.com"));
    }

    @Test
    @DisplayName("ADMIN should view faculty details with content counts")
    void adminShouldViewFacultyDetails() throws Exception {
        mockMvc.perform(get("/api/admin/faculty/" + testFacultyUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testFacultyUser.getId()))
                .andExpect(jsonPath("$.email").value(testFacultyUser.getEmail()))
                .andExpect(jsonPath("$.resourceCount").value(0))
                .andExpect(jsonPath("$.eventCount").value(0));
    }

    @Test
    @DisplayName("ADMIN should successfully create a new Faculty account")
    void adminShouldCreateFaculty() throws Exception {
        CreateFacultyRequest request = CreateFacultyRequest.builder()
                .firstName("Robert")
                .lastName("Professor")
                .email("robert.prof@example.com")
                .password("ProfPass@123")
                .phone("9876543210")
                .build();

        mockMvc.perform(post("/api/admin/faculty")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("robert.prof@example.com"))
                .andExpect(jsonPath("$.firstName").value("Robert"))
                .andExpect(jsonPath("$.password").doesNotExist());

        // Verify newly created Faculty can authenticate via standard login
        AuthResponse loginResponse = authService.login(LoginRequest.builder()
                .email("robert.prof@example.com")
                .password("ProfPass@123")
                .build());
        assertThat(loginResponse.getToken()).isNotBlank();
        assertThat(loginResponse.getRoles()).contains("FACULTY");
    }

    @Test
    @DisplayName("ADMIN creating Faculty with duplicate email should return 409 Conflict")
    void adminCreateFacultyDuplicateEmailShouldReturn409() throws Exception {
        CreateFacultyRequest request = CreateFacultyRequest.builder()
                .firstName("Duplicate")
                .lastName("User")
                .email("faculty.initial@example.com")
                .password("Password@123")
                .build();

        mockMvc.perform(post("/api/admin/faculty")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("ADMIN should edit Faculty account details")
    void adminShouldEditFaculty() throws Exception {
        UpdateFacultyRequest request = UpdateFacultyRequest.builder()
                .firstName("UpdatedInitial")
                .lastName("UpdatedFaculty")
                .phone("5551112222")
                .build();

        mockMvc.perform(put("/api/admin/faculty/" + testFacultyUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedInitial"))
                .andExpect(jsonPath("$.lastName").value("UpdatedFaculty"))
                .andExpect(jsonPath("$.phone").value("5551112222"));
    }

    @Test
    @DisplayName("ADMIN should deactivate Faculty account and prevent authentication")
    void adminShouldDeactivateFacultyAndBlockLogin() throws Exception {
        UpdateFacultyStatusRequest statusRequest = UpdateFacultyStatusRequest.builder()
                .active(false)
                .build();

        mockMvc.perform(patch("/api/admin/faculty/" + testFacultyUser.getId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // Attempting to log in as deactivated Faculty should fail
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .email("faculty.initial@example.com")
                                .password("FacultyPass@123")
                                .build())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("STUDENT accessing Admin Faculty endpoints should receive 403 Forbidden")
    void studentAccessingAdminFacultyShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/faculty")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("FACULTY accessing Admin Faculty endpoints should receive 403 Forbidden")
    void facultyAccessingAdminFacultyShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/faculty")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated request to Admin Faculty endpoints should receive 401 Unauthorized")
    void unauthenticatedAccessingAdminFacultyShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/faculty"))
                .andExpect(status().isUnauthorized());
    }
}
