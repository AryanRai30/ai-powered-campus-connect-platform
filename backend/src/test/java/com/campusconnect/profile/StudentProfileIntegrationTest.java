package com.campusconnect.profile;

import com.campusconnect.dto.AuthResponse;
import com.campusconnect.dto.RegisterRequest;
import com.campusconnect.dto.StudentProfileRequest;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class StudentProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    private String user1Token;
    private String user2Token;

    @BeforeEach
    void setUp() {
        RegisterRequest user1Req = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe.profile@example.com")
                .password("Password@123")
                .build();
        AuthResponse user1Auth = authService.register(user1Req);
        user1Token = user1Auth.getToken();

        RegisterRequest user2Req = RegisterRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith.profile@example.com")
                .password("Password@123")
                .build();
        AuthResponse user2Auth = authService.register(user2Req);
        user2Token = user2Auth.getToken();
    }

    @Test
    @DisplayName("1. Authenticated user can create a student profile (201 Created)")
    void authenticatedUserCanCreateStudentProfile() throws Exception {
        StudentProfileRequest profileReq = StudentProfileRequest.builder()
                .studentId("STU1001")
                .course("Computer Science")
                .department("School of Engineering")
                .year("3rd Year")
                .semester("5th Semester")
                .skills("Java, Spring Boot, MySQL")
                .interests("AI, Machine Learning")
                .bio("Aspiring software engineer")
                .build();

        mockMvc.perform(post("/api/student/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("john.doe.profile@example.com"))
                .andExpect(jsonPath("$.studentId").value("STU1001"))
                .andExpect(jsonPath("$.course").value("Computer Science"))
                .andExpect(jsonPath("$.department").value("School of Engineering"))
                .andExpect(jsonPath("$.year").value("3rd Year"))
                .andExpect(jsonPath("$.semester").value("5th Semester"))
                .andExpect(jsonPath("$.skills").value("Java, Spring Boot, MySQL"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("2. Authenticated user can retrieve their own profile (200 OK)")
    void authenticatedUserCanRetrieveOwnProfile() throws Exception {
        StudentProfileRequest profileReq = StudentProfileRequest.builder()
                .studentId("STU1002")
                .course("Information Technology")
                .department("School of Engineering")
                .year("2nd Year")
                .semester("3rd Semester")
                .skills("React, TypeScript")
                .interests("Web Development")
                .bio("Frontend enthusiast")
                .build();

        mockMvc.perform(post("/api/student/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/student/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value("STU1002"))
                .andExpect(jsonPath("$.email").value("john.doe.profile@example.com"))
                .andExpect(jsonPath("$.course").value("Information Technology"));
    }

    @Test
    @DisplayName("3. Authenticated user can update their own profile (200 OK)")
    void authenticatedUserCanUpdateOwnProfile() throws Exception {
        StudentProfileRequest profileReq = StudentProfileRequest.builder()
                .studentId("STU1003")
                .course("Data Science")
                .department("School of Computing")
                .year("1st Year")
                .semester("2nd Semester")
                .skills("Python, SQL")
                .interests("Data Analytics")
                .bio("Initial bio")
                .build();

        mockMvc.perform(post("/api/student/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isCreated());

        StudentProfileRequest updateReq = StudentProfileRequest.builder()
                .studentId("STU1003")
                .course("Data Science & AI")
                .department("School of Computing")
                .year("2nd Year")
                .semester("3rd Semester")
                .skills("Python, SQL, PyTorch")
                .interests("Deep Learning, Data Analytics")
                .bio("Updated bio: Deep Learning practitioner")
                .build();

        mockMvc.perform(put("/api/student/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.course").value("Data Science & AI"))
                .andExpect(jsonPath("$.year").value("2nd Year"))
                .andExpect(jsonPath("$.skills").value("Python, SQL, PyTorch"))
                .andExpect(jsonPath("$.bio").value("Updated bio: Deep Learning practitioner"));
    }

    @Test
    @DisplayName("4. Unauthenticated request to profile endpoint returns 401 Unauthorized")
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/student/profile"))
                .andExpect(status().isUnauthorized());

        StudentProfileRequest profileReq = StudentProfileRequest.builder()
                .studentId("STU9999")
                .course("Mechanical Engineering")
                .department("School of Mechanical Engineering")
                .year("4th Year")
                .semester("7th Semester")
                .build();

        mockMvc.perform(post("/api/student/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/student/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("5. Duplicate studentId is rejected (409 Conflict)")
    void duplicateStudentIdIsRejected() throws Exception {
        StudentProfileRequest profile1 = StudentProfileRequest.builder()
                .studentId("DUP-ID-001")
                .course("Electrical Engineering")
                .department("School of Electrical Engineering")
                .year("1st Year")
                .semester("1st Semester")
                .build();

        mockMvc.perform(post("/api/student/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile1)))
                .andExpect(status().isCreated());

        StudentProfileRequest profile2WithSameStudentId = StudentProfileRequest.builder()
                .studentId("DUP-ID-001")
                .course("Civil Engineering")
                .department("School of Civil Engineering")
                .year("1st Year")
                .semester("1st Semester")
                .build();

        mockMvc.perform(post("/api/student/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile2WithSameStudentId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("6. Same user cannot create two profiles (409 Conflict)")
    void sameUserCannotCreateTwoProfiles() throws Exception {
        StudentProfileRequest profile1 = StudentProfileRequest.builder()
                .studentId("USER1-STU-1")
                .course("Cybersecurity")
                .department("School of Computing")
                .year("2nd Year")
                .semester("4th Semester")
                .build();

        mockMvc.perform(post("/api/student/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile1)))
                .andExpect(status().isCreated());

        StudentProfileRequest profile2 = StudentProfileRequest.builder()
                .studentId("USER1-STU-2")
                .course("Cybersecurity")
                .department("School of Computing")
                .year("2nd Year")
                .semester("4th Semester")
                .build();

        mockMvc.perform(post("/api/student/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("7. GET when no profile exists returns 404 Not Found")
    void getNonExistentProfileReturns404() throws Exception {
        mockMvc.perform(get("/api/student/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("8. PUT when no profile exists returns 404 Not Found")
    void updateNonExistentProfileReturns404() throws Exception {
        StudentProfileRequest profileReq = StudentProfileRequest.builder()
                .studentId("STU-NOT-FOUND")
                .course("Physics")
                .department("School of Sciences")
                .year("1st Year")
                .semester("1st Semester")
                .build();

        mockMvc.perform(put("/api/student/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
