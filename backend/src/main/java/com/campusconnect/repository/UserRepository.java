package com.campusconnect.repository;

import com.campusconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE UPPER(r.name) = UPPER(:roleName)")
    long countByRoleName(@Param("roleName") String roleName);

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE UPPER(r.name) IN :roleNames")
    long countByRoleNameIn(@Param("roleNames") Collection<String> roleNames);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE UPPER(r.name) = 'FACULTY' " +
           "AND (:status IS NULL OR u.isActive = :status) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY u.id DESC")
    java.util.List<User> findFacultyUsers(@Param("status") Boolean status, @Param("search") String search);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE u.id = :id AND UPPER(r.name) = 'FACULTY'")
    Optional<User> findFacultyById(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN u.roles r
        LEFT JOIN u.studentProfile sp
        WHERE UPPER(r.name) = 'STUDENT'
          AND (:status IS NULL OR u.isActive = :status)
          AND (:dept IS NULL OR :dept = '' OR LOWER(sp.department) = LOWER(:dept))
          AND (:course IS NULL OR :course = '' OR LOWER(sp.course) = LOWER(:course))
          AND (:year IS NULL OR :year = '' OR sp.year = :year)
          AND (:sem IS NULL OR :sem = '' OR sp.semester = :sem)
          AND (:search IS NULL OR :search = '' OR
               LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(sp.studentId) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY u.id DESC
    """)
    List<User> findStudentUsers(
            @Param("status") Boolean status,
            @Param("dept") String dept,
            @Param("course") String course,
            @Param("year") String year,
            @Param("sem") String sem,
            @Param("search") String search
    );

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE u.id = :id AND UPPER(r.name) = 'STUDENT'")
    Optional<User> findStudentById(@Param("id") Long id);
}
