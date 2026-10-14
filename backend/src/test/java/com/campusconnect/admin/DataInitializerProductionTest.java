package com.campusconnect.admin;

import com.campusconnect.repository.RoleRepository;
import com.campusconnect.repository.UserRepository;
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
        "spring.datasource.url=jdbc:h2:mem:proddb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=FALSE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;NON_KEYWORDS=YEAR",
        "app.seed-dev-users=false"
})
public class DataInitializerProductionTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("In production mode, DataInitializer should seed essential roles but skip development-only default accounts")
    void verifyDevAccountsNotSeededInProduction() {
        // Essential roles must still be seeded
        assertThat(roleRepository.existsByName("STUDENT")).isTrue();
        assertThat(roleRepository.existsByName("FACULTY")).isTrue();
        assertThat(roleRepository.existsByName("ADMIN")).isTrue();
        assertThat(roleRepository.existsByName("CLUB_ADMIN")).isTrue();
        assertThat(roleRepository.existsByName("SUPER_ADMIN")).isTrue();

        // Development-only accounts must NOT be created
        assertThat(userRepository.findByEmail("admin.dev@campusconnect.edu")).isEmpty();
        assertThat(userRepository.findByEmail("faculty.dev@campusconnect.edu")).isEmpty();
    }
}
