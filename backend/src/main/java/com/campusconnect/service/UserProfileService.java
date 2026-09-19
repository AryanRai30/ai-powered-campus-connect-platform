package com.campusconnect.service;

import com.campusconnect.dto.UserProfileResponse;
import com.campusconnect.dto.UserProfileUpdateRequest;
import com.campusconnect.entity.Role;
import com.campusconnect.entity.StudentProfile;
import com.campusconnect.entity.User;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.exception.StudentProfileAlreadyExistsException;
import com.campusconnect.repository.StudentProfileRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;

    public UserProfileService(UserRepository userRepository, StudentProfileRepository studentProfileRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        return mapToResponse(user);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(UserProfileUpdateRequest request, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        // Update core user attributes
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }

        User updatedUser = userRepository.save(user);

        // Check if user has STUDENT role or has a StudentProfile
        boolean isStudent = user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(r -> r.equalsIgnoreCase("STUDENT") || r.equalsIgnoreCase("ROLE_STUDENT"));

        if (isStudent || request.getStudentId() != null) {
            Optional<StudentProfile> profileOpt = studentProfileRepository.findByUserId(user.getId());

            if (profileOpt.isPresent()) {
                StudentProfile profile = profileOpt.get();
                if (request.getStudentId() != null && !request.getStudentId().trim().isEmpty()) {
                    if (studentProfileRepository.existsByStudentIdAndUserIdNot(request.getStudentId().trim(), user.getId())) {
                        throw new StudentProfileAlreadyExistsException("Student ID '" + request.getStudentId() + "' is already registered to another user");
                    }
                    profile.setStudentId(request.getStudentId().trim());
                }
                if (request.getCourse() != null) profile.setCourse(request.getCourse().trim());
                if (request.getDepartment() != null) profile.setDepartment(request.getDepartment().trim());
                if (request.getYear() != null) profile.setYear(request.getYear().trim());
                if (request.getSemester() != null) profile.setSemester(request.getSemester().trim());
                if (request.getSkills() != null) profile.setSkills(request.getSkills().trim());
                if (request.getInterests() != null) profile.setInterests(request.getInterests().trim());
                if (request.getBio() != null) profile.setBio(request.getBio().trim());

                studentProfileRepository.save(profile);
            } else if (request.getStudentId() != null && !request.getStudentId().trim().isEmpty()) {
                if (studentProfileRepository.existsByStudentId(request.getStudentId().trim())) {
                    throw new StudentProfileAlreadyExistsException("Student ID '" + request.getStudentId() + "' is already registered");
                }
                StudentProfile newProfile = StudentProfile.builder()
                        .user(user)
                        .studentId(request.getStudentId().trim())
                        .course(request.getCourse() != null ? request.getCourse().trim() : "")
                        .department(request.getDepartment() != null ? request.getDepartment().trim() : "")
                        .year(request.getYear() != null ? request.getYear().trim() : "1st Year")
                        .semester(request.getSemester() != null ? request.getSemester().trim() : "1st Semester")
                        .skills(request.getSkills() != null ? request.getSkills().trim() : "")
                        .interests(request.getInterests() != null ? request.getInterests().trim() : "")
                        .bio(request.getBio() != null ? request.getBio().trim() : "")
                        .build();
                studentProfileRepository.save(newProfile);
            }
        }

        return mapToResponse(updatedUser);
    }

    private UserProfileResponse mapToResponse(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        UserProfileResponse.UserProfileResponseBuilder builder = UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .active(user.isActive())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        Optional<StudentProfile> studentProfileOpt = studentProfileRepository.findByUserId(user.getId());
        if (studentProfileOpt.isPresent()) {
            StudentProfile sp = studentProfileOpt.get();
            builder.studentId(sp.getStudentId())
                    .course(sp.getCourse())
                    .department(sp.getDepartment())
                    .year(sp.getYear())
                    .semester(sp.getSemester())
                    .skills(sp.getSkills())
                    .interests(sp.getInterests())
                    .bio(sp.getBio());
        }

        return builder.build();
    }
}
