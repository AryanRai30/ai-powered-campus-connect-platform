package com.campusconnect.security;

import com.campusconnect.dto.AuthResponse;
import com.campusconnect.dto.RegisterRequest;
import com.campusconnect.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    private String studentToken;

    @BeforeEach
    void setUp() {
        RegisterRequest studentRequest = RegisterRequest.builder()
                .firstName("Test")
                .lastName("StudentUser")
                .email("test.student.auth@example.com")
                .password("StudentPass@123")
                .build();

        AuthResponse authResponse = authService.register(studentRequest);
        studentToken = authResponse.getToken();
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoint should return 401 Unauthorized")
    void unauthenticatedRequestShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/protected/test"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Authenticated request with valid JWT should return 200 OK")
    void authenticatedRequestWithValidJwtShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/protected/test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value("test.student.auth@example.com"));
    }

    @Test
    @DisplayName("Student user accessing student protected endpoint should return 200 OK")
    void studentAccessingStudentEndpointShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/protected/student")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("Student user accessing faculty protected endpoint should return 403 Forbidden")
    void studentAccessingFacultyEndpointShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/protected/faculty")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Student user accessing admin protected endpoint should return 403 Forbidden")
    void studentAccessingAdminEndpointShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/protected/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }
}
