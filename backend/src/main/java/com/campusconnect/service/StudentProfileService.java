package com.campusconnect.service;

import com.campusconnect.dto.StudentProfileRequest;
import com.campusconnect.dto.StudentProfileResponse;
import com.campusconnect.entity.StudentProfile;
import com.campusconnect.entity.User;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.exception.StudentProfileAlreadyExistsException;
import com.campusconnect.repository.StudentProfileRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class handling StudentProfile business logic.
 */
@Service
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    public StudentProfileService(StudentProfileRepository studentProfileRepository, UserRepository userRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public StudentProfileResponse createProfile(StudentProfileRequest request, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        if (studentProfileRepository.existsByUserId(user.getId())) {
            throw new StudentProfileAlreadyExistsException("A student profile already exists for this user");
        }

        if (studentProfileRepository.existsByStudentId(request.getStudentId())) {
            throw new StudentProfileAlreadyExistsException("Student ID '" + request.getStudentId() + "' is already registered");
        }

        StudentProfile profile = StudentProfile.builder()
                .user(user)
                .studentId(request.getStudentId())
                .course(request.getCourse())
                .department(request.getDepartment())
                .year(request.getYear())
                .semester(request.getSemester())
                .skills(request.getSkills())
                .interests(request.getInterests())
                .bio(request.getBio())
                .build();

        StudentProfile savedProfile = studentProfileRepository.save(profile);
        return mapToResponse(savedProfile);
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getCurrentUserProfile(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        StudentProfile profile = studentProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUserEmail));

        return mapToResponse(profile);
    }

    @Transactional
    public StudentProfileResponse updateCurrentUserProfile(StudentProfileRequest request, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        StudentProfile profile = studentProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUserEmail));

        if (studentProfileRepository.existsByStudentIdAndUserIdNot(request.getStudentId(), user.getId())) {
            throw new StudentProfileAlreadyExistsException("Student ID '" + request.getStudentId() + "' is already registered to another profile");
        }

        profile.setStudentId(request.getStudentId());
        profile.setCourse(request.getCourse());
        profile.setDepartment(request.getDepartment());
        profile.setYear(request.getYear());
        profile.setSemester(request.getSemester());
        profile.setSkills(request.getSkills());
        profile.setInterests(request.getInterests());
        profile.setBio(request.getBio());

        StudentProfile updatedProfile = studentProfileRepository.save(profile);
        return mapToResponse(updatedProfile);
    }

    private StudentProfileResponse mapToResponse(StudentProfile profile) {
        User user = profile.getUser();
        return StudentProfileResponse.builder()
                .id(profile.getId())
                .userId(user != null ? user.getId() : null)
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .email(user != null ? user.getEmail() : null)
                .studentId(profile.getStudentId())
                .course(profile.getCourse())
                .department(profile.getDepartment())
                .year(profile.getYear())
                .semester(profile.getSemester())
                .skills(profile.getSkills())
                .interests(profile.getInterests())
                .bio(profile.getBio())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
