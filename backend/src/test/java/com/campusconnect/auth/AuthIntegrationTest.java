package com.campusconnect.auth;

import com.campusconnect.dto.AuthResponse;
import com.campusconnect.dto.LoginRequest;
import com.campusconnect.dto.RegisterRequest;
import com.campusconnect.entity.User;
import com.campusconnect.exception.UserAlreadyExistsException;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class AuthIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should successfully register a new student user with BCrypt hashed password and JWT")
    void shouldRegisterNewUserAndReturnJwt() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith.test@example.com")
                .password("SecurePass@123")
                .phone("9876543210")
                .build();

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getEmail()).isEqualTo("alice.smith.test@example.com");
        assertThat(response.getRoles()).contains("STUDENT");

        User savedUser = userRepository.findByEmail("alice.smith.test@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getPassword()).isNotEqualTo("SecurePass@123");
        assertThat(savedUser.getPassword()).startsWith("$2a$");
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when registering duplicate email")
    void shouldFailDuplicateRegistrationWithConflict() {
        RegisterRequest request1 = RegisterRequest.builder()
                .firstName("Bob")
                .lastName("Jones")
                .email("bob.duplicate.test@example.com")
                .password("Password@123")
                .build();

        authService.register(request1);

        RegisterRequest request2 = RegisterRequest.builder()
                .firstName("Bob")
                .lastName("Jones")
                .email("bob.duplicate.test@example.com")
                .password("Password@123")
                .build();

        assertThatThrownBy(() -> authService.register(request2))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email is already registered");
    }

    @Test
    @DisplayName("Should successfully authenticate valid credentials during login")
    void shouldLoginWithValidCredentialsAndReturnJwt() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Charlie")
                .lastName("Brown")
                .email("charlie.brown.test@example.com")
                .password("MySecret@123")
                .build();

        authService.register(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("charlie.brown.test@example.com")
                .password("MySecret@123")
                .build();

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getEmail()).isEqualTo("charlie.brown.test@example.com");
        assertThat(response.getRoles()).contains("STUDENT");
    }

    @Test
    @DisplayName("Should fail authentication when invalid password is provided")
    void shouldFailLoginWithInvalidPassword() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("David")
                .lastName("Miller")
                .email("david.miller.test@example.com")
                .password("RightPassword@123")
                .build();

        authService.register(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("david.miller.test@example.com")
                .password("WrongPassword@999")
                .build();

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }
}
