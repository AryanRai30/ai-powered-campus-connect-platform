package com.campusconnect.repository;

import com.campusconnect.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for StudentProfile entity.
 */
@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUserId(Long userId);

    Optional<StudentProfile> findByStudentId(String studentId);

    boolean existsByStudentId(String studentId);

    boolean existsByUserId(Long userId);

    boolean existsByStudentIdAndUserIdNot(String studentId, Long userId);
}
