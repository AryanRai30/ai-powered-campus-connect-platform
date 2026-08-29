package com.campusconnect.repository;

import com.campusconnect.entity.Announcement;
import com.campusconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Announcement entities.
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAllByCreatedByOrderByIdDesc(User createdBy);

    Optional<Announcement> findByIdAndCreatedBy(Long id, User createdBy);

    long countByCreatedBy(User createdBy);

    @Query("SELECT a FROM Announcement a WHERE " +
           "a.published = true AND a.active = true AND " +
           "(:category IS NULL OR :category = '' OR LOWER(a.category) = LOWER(:category)) AND " +
           "(:targetDept IS NULL OR :targetDept = '' OR a.targetDepartment IS NULL OR a.targetDepartment = '' OR LOWER(a.targetDepartment) = LOWER(:targetDept)) AND " +
           "(:targetCourse IS NULL OR :targetCourse = '' OR a.targetCourse IS NULL OR a.targetCourse = '' OR LOWER(a.targetCourse) = LOWER(:targetCourse)) AND " +
           "(:targetYear IS NULL OR :targetYear = 0 OR a.targetYear IS NULL OR a.targetYear = 0 OR a.targetYear = :targetYear) AND " +
           "(:targetSem IS NULL OR :targetSem = 0 OR a.targetSemester IS NULL OR a.targetSemester = 0 OR a.targetSemester = :targetSem) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY a.publishedAt DESC")
    List<Announcement> filterAnnouncementsForStudent(
            @Param("category") String category,
            @Param("targetDept") String targetDept,
            @Param("targetCourse") String targetCourse,
            @Param("targetYear") Integer targetYear,
            @Param("targetSem") Integer targetSem,
            @Param("search") String search
    );

    @Query("SELECT a FROM Announcement a WHERE " +
           "a.published = true AND a.active = true AND " +
           "(:category IS NULL OR :category = '' OR LOWER(a.category) = LOWER(:category)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY a.publishedAt DESC")
    List<Announcement> filterAnnouncements(@Param("category") String category, @Param("search") String search);
}
