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

    @org.springframework.data.jpa.repository.Query("""
        SELECT sp FROM StudentProfile sp
        WHERE (:dept IS NULL OR :dept = '' OR LOWER(sp.department) = LOWER(:dept))
          AND (:course IS NULL OR :course = '' OR LOWER(sp.course) = LOWER(:course))
          AND (:year IS NULL OR :year = '' OR sp.year = :year)
          AND (:sem IS NULL OR :sem = '' OR sp.semester = :sem)
          AND (:search IS NULL OR :search = '' OR
               LOWER(sp.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(sp.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(sp.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(sp.studentId) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY sp.id DESC
    """)
    java.util.List<StudentProfile> filterStudents(
            @org.springframework.data.repository.query.Param("dept") String dept,
            @org.springframework.data.repository.query.Param("course") String course,
            @org.springframework.data.repository.query.Param("year") String year,
            @org.springframework.data.repository.query.Param("sem") String sem,
            @org.springframework.data.repository.query.Param("search") String search
    );
}
