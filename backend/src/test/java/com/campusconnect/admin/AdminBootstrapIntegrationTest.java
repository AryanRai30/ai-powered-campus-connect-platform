package com.campusconnect.admin;

import com.campusconnect.config.DataInitializer;
import com.campusconnect.dto.AuthResponse;
import com.campusconnect.dto.LoginRequest;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.repository.RoleRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class AdminBootstrapIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void setUp() {
        if (!roleRepository.existsByName("ADMIN")) {
            roleRepository.save(new Role("ADMIN", "System administrator role with full administrative access"));
        }
    }

    @Test
    @DisplayName("Should create production admin account with ADMIN role and BCrypt hashed password")
    void shouldCreateAdminAccountWithValidCredentials() {
        String testEmail = "prod.admin@campusconnect.edu";
        String testPassword = "SuperAdminPassword@2026";

        MockEnvironment env = new MockEnvironment();
        env.setProperty("ADMIN_EMAIL", testEmail);
        env.setProperty("ADMIN_PASSWORD", testPassword);

        DataInitializer initializer = new DataInitializer(roleRepository, userRepository, passwordEncoder, env);
        initializer.bootstrapAdminUser();

        Optional<User> createdAdmin = userRepository.findByEmail(testEmail);
        assertThat(createdAdmin).isPresent();

        User user = createdAdmin.get();
        assertThat(user.getEmail()).isEqualTo(testEmail);
        assertThat(user.getFirstName()).isEqualTo("System");
        assertThat(user.getLastName()).isEqualTo("Admin");
        assertThat(user.isActive()).isTrue();
        assertThat(user.getRoles()).extracting(Role::getName).contains("ADMIN");

        // Verify password is BCrypt hashed and matches
        assertThat(user.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches(testPassword, user.getPassword())).isTrue();

        // Verify full login flow with AuthService succeeds
        AuthResponse response = authService.login(LoginRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .build());

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getRoles()).contains("ADMIN");
        assertThat(response.getEmail()).isEqualTo(testEmail);
    }

    @Test
    @DisplayName("Should normalize email with whitespace and mixed-case during admin bootstrap and login")
    void shouldNormalizeEmailDuringBootstrap() {
        String rawEmail = "  AdMiN.NoRmaLiZeD@CampusConnect.EDU  ";
        String normalizedEmail = "admin.normalized@campusconnect.edu";
        String testPassword = "NormalizedPass@2026";

        MockEnvironment env = new MockEnvironment();
        env.setProperty("ADMIN_EMAIL", rawEmail);
        env.setProperty("ADMIN_PASSWORD", testPassword);

        DataInitializer initializer = new DataInitializer(roleRepository, userRepository, passwordEncoder, env);
        initializer.bootstrapAdminUser();

        assertThat(userRepository.findByEmail(normalizedEmail)).isPresent();

        // Login using mixed-case and whitespace raw email
        AuthResponse response = authService.login(LoginRequest.builder()
                .email(rawEmail)
                .password(testPassword)
                .build());

        assertThat(response.getEmail()).isEqualTo(normalizedEmail);
        assertThat(response.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("Should preserve existing account and never overwrite password, roles, or details")
    void shouldPreserveExistingAccount() {
        String existingEmail = "existing.user@campusconnect.edu";
        String originalPassword = "OriginalSecretPassword@123";

        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("STUDENT", "Student role")));

        User existingUser = User.builder()
                .firstName("Existing")
                .lastName("Person")
                .email(existingEmail)
                .password(passwordEncoder.encode(originalPassword))
                .phone("1112223333")
                .isActive(true)
                .build();
        existingUser.addRole(studentRole);
        userRepository.save(existingUser);

        // Attempt admin bootstrap with same email but different password
        MockEnvironment env = new MockEnvironment();
        env.setProperty("ADMIN_EMAIL", existingEmail);
        env.setProperty("ADMIN_PASSWORD", "BrandNewPasswordAttempt@999");

        DataInitializer initializer = new DataInitializer(roleRepository, userRepository, passwordEncoder, env);
        initializer.bootstrapAdminUser();

        // Verify user was NOT modified
        User preservedUser = userRepository.findByEmail(existingEmail).orElseThrow();
        assertThat(preservedUser.getFirstName()).isEqualTo("Existing");
        assertThat(preservedUser.getLastName()).isEqualTo("Person");
        assertThat(passwordEncoder.matches(originalPassword, preservedUser.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("BrandNewPasswordAttempt@999", preservedUser.getPassword())).isFalse();
        assertThat(preservedUser.getRoles()).extracting(Role::getName).contains("STUDENT");
        assertThat(preservedUser.getRoles()).extracting(Role::getName).doesNotContain("ADMIN");
    }

    @Test
    @DisplayName("Should gracefully skip bootstrap when neither ADMIN_EMAIL nor ADMIN_PASSWORD is provided")
    void shouldGracefullySkipWhenConfigurationMissing() {
        MockEnvironment env = new MockEnvironment();
        // Neither property is set

        DataInitializer initializer = new DataInitializer(roleRepository, userRepository, passwordEncoder, env);
        initializer.bootstrapAdminUser();

        // No exception thrown, and nonexistent email is not created
        assertThat(userRepository.findByEmail("admin.nonexistent@campusconnect.edu")).isEmpty();
    }

    @Test
    @DisplayName("Should fail clearly when only ADMIN_EMAIL is configured without ADMIN_PASSWORD")
    void shouldFailWhenOnlyEmailIsConfigured() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("ADMIN_EMAIL", "admin.only@campusconnect.edu");
        // ADMIN_PASSWORD is null or empty

        DataInitializer initializer = new DataInitializer(roleRepository, userRepository, passwordEncoder, env);

        assertThatThrownBy(initializer::bootstrapAdminUser)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADMIN_PASSWORD is missing or blank");
    }

    @Test
    @DisplayName("Should fail clearly when only ADMIN_PASSWORD is configured without ADMIN_EMAIL")
    void shouldFailWhenOnlyPasswordIsConfigured() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("ADMIN_PASSWORD", "SomePassword123");
        // ADMIN_EMAIL is null or empty

        DataInitializer initializer = new DataInitializer(roleRepository, userRepository, passwordEncoder, env);

        assertThatThrownBy(initializer::bootstrapAdminUser)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADMIN_EMAIL is missing or blank");
    }

    @Test
    @DisplayName("Should fail clearly when ADMIN_EMAIL has an invalid format")
    void shouldFailWhenEmailFormatIsInvalid() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("ADMIN_EMAIL", "invalid-email-address");
        env.setProperty("ADMIN_PASSWORD", "ValidPassword123");

        DataInitializer initializer = new DataInitializer(roleRepository, userRepository, passwordEncoder, env);

        assertThatThrownBy(initializer::bootstrapAdminUser)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("invalid email format");
    }

    @Test
    @DisplayName("Should fail clearly when ADMIN_PASSWORD is shorter than 6 characters")
    void shouldFailWhenPasswordIsTooShort() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("ADMIN_EMAIL", "admin.short@campusconnect.edu");
        env.setProperty("ADMIN_PASSWORD", "12345");

        DataInitializer initializer = new DataInitializer(roleRepository, userRepository, passwordEncoder, env);

        assertThatThrownBy(initializer::bootstrapAdminUser)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be at least 6 characters long");
    }

    @Test
    @DisplayName("Should support property resolution via app.admin.email and app.admin.password")
    void shouldSupportAppAdminPropertiesResolution() {
        String testEmail = "app.admin@campusconnect.edu";
        String testPassword = "AppAdminPassword@2026";

        MockEnvironment env = new MockEnvironment();
        env.setProperty("app.admin.email", testEmail);
        env.setProperty("app.admin.password", testPassword);

        DataInitializer initializer = new DataInitializer(roleRepository, userRepository, passwordEncoder, env);
        initializer.bootstrapAdminUser();

        assertThat(userRepository.findByEmail(testEmail)).isPresent();
    }
}
