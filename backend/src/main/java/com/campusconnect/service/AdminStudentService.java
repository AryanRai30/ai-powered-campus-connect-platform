package com.campusconnect.service;

import com.campusconnect.dto.AdminStudentDetailResponse;
import com.campusconnect.dto.AdminStudentResponse;
import com.campusconnect.entity.StudentProfile;
import com.campusconnect.entity.User;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling Administrator Student Account Management workflows.
 */
@Service
public class AdminStudentService {

    private final UserRepository userRepository;
    private final com.campusconnect.repository.StudentProfileRepository studentProfileRepository;

    public AdminStudentService(UserRepository userRepository, com.campusconnect.repository.StudentProfileRepository studentProfileRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
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
    public List<AdminStudentResponse> getStudentList(
            String adminEmail,
            String status,
            String dept,
            String course,
            String year,
            String sem,
            String search
    ) {
        getAuthenticatedAdmin(adminEmail);

        Boolean statusBool = null;
        if (status != null && !status.trim().isEmpty()) {
            if ("active".equalsIgnoreCase(status.trim())) {
                statusBool = true;
            } else if ("inactive".equalsIgnoreCase(status.trim())) {
                statusBool = false;
            }
        }

        List<User> students = userRepository.findStudentUsers(statusBool, dept, course, year, sem, search);

        return students.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminStudentDetailResponse getStudentById(String adminEmail, Long id) {
        getAuthenticatedAdmin(adminEmail);

        User student = userRepository.findStudentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student user not found with ID: " + id));

        StudentProfile sp = student.getStudentProfile();
        if (sp == null) {
            sp = studentProfileRepository.findByUserId(student.getId()).orElse(null);
        }

        return AdminStudentDetailResponse.builder()
                .id(student.getId())
                .studentId(sp != null ? sp.getStudentId() : null)
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .department(sp != null ? sp.getDepartment() : null)
                .course(sp != null ? sp.getCourse() : null)
                .year(sp != null ? sp.getYear() : null)
                .semester(sp != null ? sp.getSemester() : null)
                .skills(sp != null ? sp.getSkills() : null)
                .interests(sp != null ? sp.getInterests() : null)
                .bio(sp != null ? sp.getBio() : null)
                .active(student.isActive())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }

    @Transactional
    public AdminStudentResponse updateStudentStatus(String adminEmail, Long id, boolean active) {
        getAuthenticatedAdmin(adminEmail);

        User student = userRepository.findStudentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student user not found with ID: " + id));

        student.setActive(active);
        User updatedUser = userRepository.save(student);

        return mapToResponse(updatedUser);
    }

    private AdminStudentResponse mapToResponse(User user) {
        StudentProfile sp = user.getStudentProfile();
        if (sp == null) {
            sp = studentProfileRepository.findByUserId(user.getId()).orElse(null);
        }
        return AdminStudentResponse.builder()
                .id(user.getId())
                .studentId(sp != null ? sp.getStudentId() : null)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .department(sp != null ? sp.getDepartment() : null)
                .course(sp != null ? sp.getCourse() : null)
                .year(sp != null ? sp.getYear() : null)
                .semester(sp != null ? sp.getSemester() : null)
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
