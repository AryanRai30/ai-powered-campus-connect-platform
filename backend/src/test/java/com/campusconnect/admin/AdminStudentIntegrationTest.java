package com.campusconnect.admin;

import com.campusconnect.dto.AuthResponse;
import com.campusconnect.dto.LoginRequest;
import com.campusconnect.dto.RegisterRequest;
import com.campusconnect.dto.StudentProfileRequest;
import com.campusconnect.dto.UpdateStudentStatusRequest;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.repository.RoleRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.AuthService;
import com.campusconnect.service.StudentProfileService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminStudentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private StudentProfileService studentProfileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String facultyToken;
    private String studentToken;
    private String studentEmail;
    private Long studentUserId;

    @BeforeEach
    void setUp() {
        // Create Admin user
        String adminEmail = "admin.studenttest@example.com";
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Admin role")));
        User adminUser = User.builder()
                .firstName("Admin")
                .lastName("Tester")
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

        // Create Faculty user
        String facultyEmail = "faculty.studenttest@example.com";
        Role facultyRole = roleRepository.findByName("FACULTY")
                .orElseGet(() -> roleRepository.save(new Role("FACULTY", "Faculty role")));
        User facultyUser = User.builder()
                .firstName("Faculty")
                .lastName("Tester")
                .email(facultyEmail)
                .password(passwordEncoder.encode("FacultyPass@123"))
                .isActive(true)
                .build();
        facultyUser.addRole(facultyRole);
        userRepository.save(facultyUser);

        AuthResponse facultyAuth = authService.login(LoginRequest.builder()
                .email(facultyEmail)
                .password("FacultyPass@123")
                .build());
        facultyToken = facultyAuth.getToken();

        // Register Student
        studentEmail = "bob.student.test@example.com";
        AuthResponse studentAuth = authService.register(RegisterRequest.builder()
                .firstName("Bob")
                .lastName("Student")
                .email(studentEmail)
                .password("StudentPass@123")
                .build());
        studentToken = studentAuth.getToken();
        studentUserId = studentAuth.getUserId();

        // Create Profile for Student
        studentProfileService.createProfile(StudentProfileRequest.builder()
                .studentId("STU-1002")
                .department("Computer Science & Engineering")
                .course("B.Tech")
                .year("3rd Year")
                .semester("6th Semester")
                .skills("Java, React")
                .interests("AI, Web Dev")
                .bio("Computer Science Student")
                .build(), studentEmail);
    }

    @Test
    @DisplayName("ADMIN should list students with profile info and search/filters")
    void adminShouldListStudents() throws Exception {
        mockMvc.perform(get("/api/admin/students")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .param("dept", "Computer Science & Engineering")
                        .param("search", "Bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(notNullValue())))
                .andExpect(jsonPath("$[0].email").value(studentEmail))
                .andExpect(jsonPath("$[0].studentId").value("STU-1002"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    @DisplayName("ADMIN should view student details including skills, interests, and bio")
    void adminShouldViewStudentDetails() throws Exception {
        mockMvc.perform(get("/api/admin/students/" + studentUserId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentUserId))
                .andExpect(jsonPath("$.email").value(studentEmail))
                .andExpect(jsonPath("$.studentId").value("STU-1002"))
                .andExpect(jsonPath("$.department").value("Computer Science & Engineering"))
                .andExpect(jsonPath("$.skills").value("Java, React"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("ADMIN should deactivate student account and block login, then reactivate")
    void adminShouldDeactivateAndReactivateStudent() throws Exception {
        // Deactivate Student
        UpdateStudentStatusRequest deactivateReq = UpdateStudentStatusRequest.builder()
                .active(false)
                .build();

        mockMvc.perform(patch("/api/admin/students/" + studentUserId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deactivateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // Attempt student login -> 401 Unauthorized
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .email(studentEmail)
                                .password("StudentPass@123")
                                .build())))
                .andExpect(status().isUnauthorized());

        // Reactivate Student
        UpdateStudentStatusRequest activateReq = UpdateStudentStatusRequest.builder()
                .active(true)
                .build();

        mockMvc.perform(patch("/api/admin/students/" + studentUserId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        // Attempt student login -> 200 OK
        AuthResponse loginResp = authService.login(LoginRequest.builder()
                .email(studentEmail)
                .password("StudentPass@123")
                .build());
        assertThat(loginResp.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("STUDENT accessing Admin Student endpoints should receive 403 Forbidden")
    void studentAccessingAdminStudentShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/students")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("FACULTY accessing Admin Student endpoints should receive 403 Forbidden")
    void facultyAccessingAdminStudentShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/students")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + facultyToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated request to Admin Student endpoints should receive 401 Unauthorized")
    void unauthenticatedAccessingAdminStudentShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/students"))
                .andExpect(status().isUnauthorized());
    }
}
