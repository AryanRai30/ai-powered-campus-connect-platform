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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
@TestPropertySource(properties = {
        "ADMIN_EMAIL=production.bootstrap.admin@campusconnect.edu",
        "ADMIN_PASSWORD=ProdBootstrapPass@2026",
        "app.seed-dev-users=false"
})
public class AdminBootstrapApplicationContextTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("Spring Boot context startup automatically bootstraps production admin when properties are set")
    void shouldAutomaticallyBootstrapAdminOnStartup() {
        User adminUser = userRepository.findByEmail("production.bootstrap.admin@campusconnect.edu")
                .orElseThrow(() -> new AssertionError("Bootstrapped admin was not found"));

        assertThat(adminUser.getRoles()).extracting("name").contains("ADMIN");
        assertThat(adminUser.getPassword()).startsWith("$2");

        // Verify login
        AuthResponse loginResponse = authService.login(LoginRequest.builder()
                .email("production.bootstrap.admin@campusconnect.edu")
                .password("ProdBootstrapPass@2026")
                .build());

        assertThat(loginResponse.getToken()).isNotBlank();
        assertThat(loginResponse.getRoles()).contains("ADMIN");

        // Dev users must still be skipped because app.seed-dev-users=false
        assertThat(userRepository.findByEmail("admin.dev@campusconnect.edu")).isEmpty();
    }
}
