package com.campusconnect.security;

import com.campusconnect.dto.AuthResponse;
import com.campusconnect.dto.FacultyResourceRequest;
import com.campusconnect.dto.LoginRequest;
import com.campusconnect.dto.RegisterRequest;
import com.campusconnect.dto.UpdateStudentStatusRequest;
import com.campusconnect.entity.AcademicResource;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.repository.AcademicResourceRepository;
import com.campusconnect.repository.RoleRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.AuthService;
import com.campusconnect.service.FacultyResourceService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RoleAccessSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private FacultyResourceService facultyResourceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AcademicResourceRepository resourceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String facultyTokenA;
    private String facultyTokenB;
    private String studentToken;
    private User facultyUserA;
    private User facultyUserB;
    private User studentUser;

    @BeforeEach
    void setUp() {
        // 1. Create Admin User
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "System admin")));
        User admin = User.builder()
                .firstName("Super")
                .lastName("Admin")
                .email("role.admin.test@campusconnect.edu")
                .password(passwordEncoder.encode("AdminPass@123"))
                .isActive(true)
                .build();
        admin.addRole(adminRole);
        userRepository.save(admin);

        AuthResponse adminAuth = authService.login(LoginRequest.builder()
                .email("role.admin.test@campusconnect.edu")
                .password("AdminPass@123")
                .build());
        adminToken = adminAuth.getToken();

        // 2. Create Faculty A User
        Role facultyRole = roleRepository.findByName("FACULTY")
                .orElseGet(() -> roleRepository.save(new Role("FACULTY", "Faculty role")));
        facultyUserA = User.builder()
                .firstName("Faculty")
                .lastName("Alpha")
                .email("faculty.alpha@campusconnect.edu")
                .password(passwordEncoder.encode("FacultyPass@123"))
                .isActive(true)
                .build();
        facultyUserA.addRole(facultyRole);
        userRepository.save(facultyUserA);

        AuthResponse facultyAuthA = authService.login(LoginRequest.builder()
                .email("faculty.alpha@campusconnect.edu")
                .password("FacultyPass@123")
                .build());
        facultyTokenA = facultyAuthA.getToken();

        // 3. Create Faculty B User
        facultyUserB = User.builder()
                .firstName("Faculty")
                .lastName("Beta")
                .email("faculty.beta@campusconnect.edu")
                .password(passwordEncoder.encode("FacultyPass@123"))
                .isActive(true)
                .build();
        facultyUserB.addRole(facultyRole);
        userRepository.save(facultyUserB);

        AuthResponse facultyAuthB = authService.login(LoginRequest.builder()
                .email("faculty.beta@campusconnect.edu")
                .password("FacultyPass@123")
                .build());
        facultyTokenB = facultyAuthB.getToken();

        // 4. Register Student User
        AuthResponse studentAuth = authService.register(RegisterRequest.builder()
                .firstName("Student")
                .lastName("Gamma")
                .email("student.gamma@campusconnect.edu")
                .password("StudentPass@123")
                .build());
        studentToken = studentAuth.getToken();
        studentUser = userRepository.findByEmail("student.gamma@campusconnect.edu").orElseThrow();
    }

    @Test
    @DisplayName("ADMIN should access Admin Dashboard, Faculty Management, and Student Management APIs")
    void adminShouldAccessAllAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").exists());

        mockMvc.perform(get("/api/admin/faculty")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/students")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("FACULTY attempting to access Admin APIs should receive 403 Forbidden")
    void facultyAccessingAdminApisShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenA))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/faculty")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenA))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/students")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("STUDENT attempting to access Admin APIs should receive 403 Forbidden")
    void studentAccessingAdminApisShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/faculty")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/students")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated request to Admin APIs should receive 401 Unauthorized")
    void unauthenticatedAccessingAdminApisShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/faculty"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Public Registration must strictly assign STUDENT role (Privilege Escalation Protection)")
    void publicRegistrationMustAssignOnlyStudentRole() throws Exception {
        RegisterRequest registerReq = RegisterRequest.builder()
                .firstName("New")
                .lastName("Registrant")
                .email("new.registrant@campusconnect.edu")
                .password("UserPass@123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles[0]").value("STUDENT"))
                .andExpect(jsonPath("$.roles").value(org.hamcrest.Matchers.hasSize(1)));

        User created = userRepository.findByEmail("new.registrant@campusconnect.edu").orElseThrow();
        assertThat(created.getRoles()).hasSize(1);
        assertThat(created.getRoles().iterator().next().getName()).isEqualTo("STUDENT");
    }

    @Test
    @DisplayName("Deactivated student account cannot authenticate (401 Unauthorized)")
    void deactivatedStudentAccountCannotAuthenticate() throws Exception {
        // Deactivate student account as Admin
        UpdateStudentStatusRequest statusReq = UpdateStudentStatusRequest.builder()
                .active(false)
                .build();

        mockMvc.perform(patch("/api/admin/students/" + studentUser.getId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // Attempt login as deactivated student -> should fail with 401
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .email("student.gamma@campusconnect.edu")
                                .password("StudentPass@123")
                                .build())))
                .andExpect(status().isUnauthorized());

        // Re-activate student account as Admin
        mockMvc.perform(patch("/api/admin/students/" + studentUser.getId() + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateStudentStatusRequest.builder().active(true).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        // Attempt login as re-activated student -> should succeed
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .email("student.gamma@campusconnect.edu")
                                .password("StudentPass@123")
                                .build())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Faculty A cannot edit or delete Faculty B's content (Faculty Ownership Security)")
    void facultyACannotEditOrDeleteFacultyBContent() throws Exception {
        // Faculty A creates a resource
        FacultyResourceRequest resourceReq = FacultyResourceRequest.builder()
                .title("Alpha Data Structures Notes")
                .description("Exclusive CS notes")
                .category("Notes")
                .subject("Computer Science")
                .resourceType("PDF")
                .resourceUrl("https://example.com/alpha.pdf")
                .published(true)
                .build();

        var createdResource = facultyResourceService.createResource(resourceReq, "faculty.alpha@campusconnect.edu");
        Long resourceId = createdResource.getId();

        // Faculty B attempts to update Faculty A's resource -> should be Forbidden (403)
        FacultyResourceRequest updateReq = FacultyResourceRequest.builder()
                .title("Tampered Notes")
                .description("Hacked description")
                .category("Notes")
                .subject("Computer Science")
                .resourceType("PDF")
                .resourceUrl("https://example.com/hacked.pdf")
                .build();

        mockMvc.perform(put("/api/faculty/resources/" + resourceId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());

        // Faculty B attempts to delete Faculty A's resource -> should be Forbidden (403)
        mockMvc.perform(delete("/api/faculty/resources/" + resourceId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyTokenB))
                .andExpect(status().isForbidden());

        // Verify resource was not altered
        AcademicResource resourceInDb = resourceRepository.findById(resourceId).orElseThrow();
        assertThat(resourceInDb.getTitle()).isEqualTo("Alpha Data Structures Notes");
    }

    @Test
    @DisplayName("Admin APIs must never expose sensitive credentials or password hashes")
    void adminApisMustNotExposeSensitiveData() throws Exception {
        mockMvc.perform(get("/api/admin/faculty")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());

        mockMvc.perform(get("/api/admin/students")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }
}
