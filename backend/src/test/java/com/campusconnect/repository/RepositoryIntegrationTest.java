package com.campusconnect.repository;

import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class RepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Should retrieve seeded roles and save custom Role by name")
    void shouldSaveAndRetrieveRole() {
        Role role = Role.builder()
                .name("TEST_ROLE")
                .description("Test role description")
                .build();

        Role savedRole = roleRepository.save(role);

        assertThat(savedRole.getId()).isNotNull();
        Optional<Role> found = roleRepository.findByName("TEST_ROLE");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("TEST_ROLE");
    }

    @Test
    @DisplayName("Should save User with Role and populate timestamps")
    void shouldSaveUserWithRoleAndPopulateTimestamps() {
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("STUDENT")
                        .description("Student Role")
                        .build()));

        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe.test@example.com")
                .password("plain_password_for_foundation")
                .phone("1234567890")
                .isActive(true)
                .build();

        user.addRole(studentRole);

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.getRoles()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(userRepository.existsByEmail("john.doe.test@example.com")).isTrue();
    }
}
