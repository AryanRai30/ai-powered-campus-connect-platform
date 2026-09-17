package com.campusconnect.config;

import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.repository.RoleRepository;
import com.campusconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Initializes essential system seed roles (STUDENT, FACULTY, ADMIN, CLUB_ADMIN, SUPER_ADMIN)
 * and development user accounts cleanly without modifying or purging real campus database content.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedDevFacultyUser();
        seedDevAdminUser();
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
