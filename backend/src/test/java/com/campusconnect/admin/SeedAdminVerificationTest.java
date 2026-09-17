package com.campusconnect.admin;

import com.campusconnect.dto.AuthResponse;
import com.campusconnect.dto.LoginRequest;
import com.campusconnect.entity.User;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SeedAdminVerificationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("DataInitializer should seed admin.dev@campusconnect.edu and enable authentication")
    void verifyDevAdminSeededAndAuthenticates() {
        User adminUser = userRepository.findByEmail("admin.dev@campusconnect.edu")
                .orElseThrow(() -> new AssertionError("admin.dev@campusconnect.edu was not seeded by DataInitializer"));

        assertThat(adminUser.getEmail()).isEqualTo("admin.dev@campusconnect.edu");
        assertThat(adminUser.getFirstName()).isEqualTo("System");
        assertThat(adminUser.getLastName()).isEqualTo("Admin");
        assertThat(adminUser.isActive()).isTrue();
        assertThat(adminUser.getRoles()).extracting("name").contains("ADMIN");
        assertThat(adminUser.getPassword()).startsWith("$2");

        // Authenticate via AuthService login
        AuthResponse loginResponse = authService.login(LoginRequest.builder()
                .email("admin.dev@campusconnect.edu")
                .password("AdminPass@123")
                .build());

        assertThat(loginResponse.getToken()).isNotBlank();
        assertThat(loginResponse.getRoles()).contains("ADMIN");
    }
}
