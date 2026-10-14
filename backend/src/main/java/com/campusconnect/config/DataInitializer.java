package com.campusconnect.config;

import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.repository.RoleRepository;
import com.campusconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Initializes essential system seed roles (STUDENT, FACULTY, ADMIN, CLUB_ADMIN, SUPER_ADMIN),
 * optional production admin user bootstrap from environment variables (ADMIN_EMAIL, ADMIN_PASSWORD),
 * and development user accounts cleanly without modifying or purging real campus database content.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$");

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public DataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Environment environment
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        bootstrapAdminUser();
        if (shouldSeedDevUsers()) {
            seedDevFacultyUser();
            seedDevAdminUser();
        } else {
            logger.info("DataInitializer: Skipping development user accounts in production environment.");
        }
    }

    /**
     * Bootstraps a production administrator user if ADMIN_EMAIL and ADMIN_PASSWORD
     * environment variables or application properties are configured.
     *
     * Validates both variables, ensures the ADMIN role exists, hashes the password with BCrypt,
     * and guarantees that existing user records, passwords, and roles are never overwritten.
     */
    @Transactional
    public void bootstrapAdminUser() {
        String adminEmail = environment.getProperty("app.admin.email");
        if (adminEmail == null || adminEmail.isBlank()) {
            adminEmail = environment.getProperty("ADMIN_EMAIL");
        }

        String adminPassword = environment.getProperty("app.admin.password");
        if (adminPassword == null || adminPassword.isBlank()) {
            adminPassword = environment.getProperty("ADMIN_PASSWORD");
        }

        boolean hasEmail = adminEmail != null && !adminEmail.trim().isEmpty();
        boolean hasPassword = adminPassword != null && !adminPassword.trim().isEmpty();

        // If neither variable is configured, skip bootstrap gracefully
        if (!hasEmail && !hasPassword) {
            logger.debug("Admin bootstrap skipped: ADMIN_EMAIL and ADMIN_PASSWORD are not configured.");
            return;
        }

        // Fail clearly and safely if only one variable is configured
        if (hasEmail && !hasPassword) {
            throw new IllegalStateException("Production admin bootstrap configuration error: ADMIN_EMAIL is configured, but ADMIN_PASSWORD is missing or blank.");
        }
        if (!hasEmail && hasPassword) {
            throw new IllegalStateException("Production admin bootstrap configuration error: ADMIN_PASSWORD is configured, but ADMIN_EMAIL is missing or blank.");
        }

        // Validate normalized email format and length
        String normalizedEmail = adminEmail.trim().toLowerCase();
        if (normalizedEmail.length() > 100 || !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalStateException("Production admin bootstrap configuration error: ADMIN_EMAIL has an invalid email format: " + normalizedEmail);
        }

        // Validate password constraints (at least 6 characters, max 100 characters)
        if (adminPassword.length() < 6) {
            throw new IllegalStateException("Production admin bootstrap configuration error: ADMIN_PASSWORD must be at least 6 characters long.");
        }
        if (adminPassword.length() > 100) {
            throw new IllegalStateException("Production admin bootstrap configuration error: ADMIN_PASSWORD cannot exceed 100 characters.");
        }

        // Check if the account already exists; if so, never overwrite or reset password/roles
        if (userRepository.existsByEmail(normalizedEmail)) {
            logger.info("Admin bootstrap: Account already exists for email: {}. Skipping bootstrap to preserve existing user credentials and roles.", normalizedEmail);
            return;
        }

        // Ensure the ADMIN role exists
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN", "System administrator role with full administrative access")));

        // Create and persist the production administrator
        User adminUser = User.builder()
                .firstName("System")
                .lastName("Admin")
                .email(normalizedEmail)
                .password(passwordEncoder.encode(adminPassword))
                .phone("5550188888")
                .isActive(true)
                .build();

        adminUser.addRole(adminRole);
        userRepository.save(adminUser);
        logger.info("Admin bootstrap: Successfully initialized production admin account for email: {}", normalizedEmail);
    }

    /**
     * Determines whether development seed accounts (faculty.dev, admin.dev) should be initialized.
     * Automatically disabled in production profile or Render cloud environment unless explicitly enabled.
     */
    private boolean shouldSeedDevUsers() {
        String seedConfig = environment.getProperty("app.seed-dev-users");
        if (seedConfig != null && !seedConfig.trim().isEmpty()) {
            return Boolean.parseBoolean(seedConfig.trim());
        }

        // Disable automatically if prod or production profile is active
        if (environment.acceptsProfiles(Profiles.of("prod", "production"))) {
            return false;
        }

        // Disable automatically when running in Render cloud runtime environment
        if ("true".equalsIgnoreCase(environment.getProperty("RENDER"))) {
            return false;
        }

        return true;
    }

    private void seedRoles() {
        try {
            List<Role> defaultRoles = List.of(
                new Role("STUDENT", "Student role with access to academic and campus features"),
                new Role("FACULTY", "Faculty role for managing courses and academic resources"),
                new Role("ADMIN", "System administrator role with full administrative access"),
                new Role("CLUB_ADMIN", "Club administrator role for event and activity management"),
                new Role("SUPER_ADMIN", "Super administrator role with full system privileges")
            );

            for (Role role : defaultRoles) {
                if (!roleRepository.existsByName(role.getName())) {
                    roleRepository.save(role);
                    logger.info("Initialized default role: {}", role.getName());
                }
            }
        } catch (Exception e) {
            logger.warn("DataInitializer skipped role seeding: {}", e.getMessage());
        }
    }

    private void seedDevFacultyUser() {
        try {
            String devFacultyEmail = "faculty.dev@campusconnect.edu";
            if (!userRepository.existsByEmail(devFacultyEmail)) {
                Role facultyRole = roleRepository.findByName("FACULTY")
                        .orElseGet(() -> roleRepository.save(new Role("FACULTY", "Faculty role for managing courses and academic resources")));

                User facultyUser = User.builder()
                        .firstName("Faculty")
                        .lastName("Member")
                        .email(devFacultyEmail)
                        .password(passwordEncoder.encode("FacultyPass@123"))
                        .phone("5550199999")
                        .isActive(true)
                        .build();

                facultyUser.addRole(facultyRole);
                userRepository.save(facultyUser);
                logger.info("Initialized default development faculty user: {}", devFacultyEmail);
            }
        } catch (Exception e) {
            logger.warn("DataInitializer skipped faculty user seeding: {}", e.getMessage());
        }
    }

    private void seedDevAdminUser() {
        try {
            String devAdminEmail = "admin.dev@campusconnect.edu";
            if (!userRepository.existsByEmail(devAdminEmail)) {
                Role adminRole = roleRepository.findByName("ADMIN")
                        .orElseGet(() -> roleRepository.save(new Role("ADMIN", "System administrator role with full administrative access")));

                User adminUser = User.builder()
                        .firstName("System")
                        .lastName("Admin")
                        .email(devAdminEmail)
                        .password(passwordEncoder.encode("AdminPass@123"))
                        .phone("5550188888")
                        .isActive(true)
                        .build();

                adminUser.addRole(adminRole);
                userRepository.save(adminUser);
                logger.info("Initialized default development admin user: {}", devAdminEmail);
            }
        } catch (Exception e) {
            logger.warn("DataInitializer skipped admin user seeding: {}", e.getMessage());
        }
    }
}
