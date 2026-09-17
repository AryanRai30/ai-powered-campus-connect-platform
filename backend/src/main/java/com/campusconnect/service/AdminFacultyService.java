package com.campusconnect.service;

import com.campusconnect.dto.AdminFacultyDetailResponse;
import com.campusconnect.dto.AdminFacultyResponse;
import com.campusconnect.dto.CreateFacultyRequest;
import com.campusconnect.dto.UpdateFacultyRequest;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.User;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.exception.UserAlreadyExistsException;
import com.campusconnect.repository.AcademicResourceRepository;
import com.campusconnect.repository.AnnouncementRepository;
import com.campusconnect.repository.ClubRepository;
import com.campusconnect.repository.EventRepository;
import com.campusconnect.repository.OpportunityRepository;
import com.campusconnect.repository.RoleRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service providing Administrator Faculty Management workflows.
 */
@Service
public class AdminFacultyService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AcademicResourceRepository resourceRepository;
    private final AnnouncementRepository announcementRepository;
    private final EventRepository eventRepository;
    private final ClubRepository clubRepository;
    private final OpportunityRepository opportunityRepository;

    public AdminFacultyService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AcademicResourceRepository resourceRepository,
            AnnouncementRepository announcementRepository,
            EventRepository eventRepository,
            ClubRepository clubRepository,
            OpportunityRepository opportunityRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.resourceRepository = resourceRepository;
        this.announcementRepository = announcementRepository;
        this.eventRepository = eventRepository;
        this.clubRepository = clubRepository;
        this.opportunityRepository = opportunityRepository;
    }

    private User getAuthenticatedAdmin(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getName()) ||
                               "SUPER_ADMIN".equalsIgnoreCase(r.getName()) ||
                               "CLUB_ADMIN".equalsIgnoreCase(r.getName()));

        if (!isAdmin) {
            throw new AccessDeniedException("User does not possess ADMIN privileges.");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public List<AdminFacultyResponse> getFacultyList(String adminEmail, String status, String search) {
        getAuthenticatedAdmin(adminEmail);

        Boolean statusBool = null;
        if (status != null && !status.trim().isEmpty()) {
            if ("active".equalsIgnoreCase(status.trim())) {
                statusBool = true;
            } else if ("inactive".equalsIgnoreCase(status.trim())) {
                statusBool = false;
            }
        }

        List<User> facultyUsers = userRepository.findFacultyUsers(statusBool, search);

        return facultyUsers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminFacultyDetailResponse getFacultyById(String adminEmail, Long id) {
        getAuthenticatedAdmin(adminEmail);

        User faculty = userRepository.findFacultyById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty user not found with ID: " + id));

        long resourceCount = resourceRepository.countByCreatedBy(faculty);
        long announcementCount = announcementRepository.countByCreatedBy(faculty);
        long eventCount = eventRepository.countByCreatedBy(faculty);
        long clubCount = clubRepository.countByCreatedBy(faculty);
        long opportunityCount = opportunityRepository.countByCreatedBy(faculty);

        return AdminFacultyDetailResponse.builder()
                .id(faculty.getId())
                .firstName(faculty.getFirstName())
                .lastName(faculty.getLastName())
                .email(faculty.getEmail())
                .phone(faculty.getPhone())
                .active(faculty.isActive())
                .createdAt(faculty.getCreatedAt())
                .updatedAt(faculty.getUpdatedAt())
                .resourceCount(resourceCount)
                .announcementCount(announcementCount)
                .eventCount(eventCount)
                .clubCount(clubCount)
                .opportunityCount(opportunityCount)
                .build();
    }

    @Transactional
    public AdminFacultyResponse createFaculty(String adminEmail, CreateFacultyRequest request) {
        getAuthenticatedAdmin(adminEmail);

        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User already exists with email: " + email);
        }

        Role facultyRole = roleRepository.findByName("FACULTY")
                .orElseGet(() -> roleRepository.save(new Role("FACULTY", "Faculty role for managing courses and academic resources")));

        User facultyUser = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .isActive(true)
                .build();

        facultyUser.addRole(facultyRole);
        User savedUser = userRepository.save(facultyUser);

        return mapToResponse(savedUser);
    }

    @Transactional
    public AdminFacultyResponse updateFaculty(String adminEmail, Long id, UpdateFacultyRequest request) {
        getAuthenticatedAdmin(adminEmail);

        User faculty = userRepository.findFacultyById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty user not found with ID: " + id));

        faculty.setFirstName(request.getFirstName().trim());
        faculty.setLastName(request.getLastName().trim());
        if (request.getPhone() != null) {
            faculty.setPhone(request.getPhone().trim());
        }

        User updatedUser = userRepository.save(faculty);
        return mapToResponse(updatedUser);
    }

    @Transactional
    public AdminFacultyResponse updateFacultyStatus(String adminEmail, Long id, boolean active) {
        getAuthenticatedAdmin(adminEmail);

        User faculty = userRepository.findFacultyById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty user not found with ID: " + id));

        faculty.setActive(active);
        User updatedUser = userRepository.save(faculty);
        return mapToResponse(updatedUser);
    }

    private AdminFacultyResponse mapToResponse(User user) {
        return AdminFacultyResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
