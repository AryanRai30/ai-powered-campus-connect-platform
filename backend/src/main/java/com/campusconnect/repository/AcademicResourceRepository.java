package com.campusconnect.repository;

import com.campusconnect.entity.AcademicResource;
import com.campusconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for AcademicResource entities.
 */
@Repository
public interface AcademicResourceRepository extends JpaRepository<AcademicResource, Long> {

    List<AcademicResource> findAllByCreatedByOrderByIdDesc(User createdBy);

    Optional<AcademicResource> findByIdAndCreatedBy(Long id, User createdBy);

    long countByCreatedBy(User createdBy);

    @Query("SELECT r FROM AcademicResource r WHERE " +
           "r.published = true AND r.active = true AND " +
           "(:category IS NULL OR :category = '' OR LOWER(r.category) = LOWER(:category)) AND " +
           "(:subject IS NULL OR :subject = '' OR LOWER(r.subject) = LOWER(:subject)) AND " +
           "(:resourceType IS NULL OR :resourceType = '' OR UPPER(r.resourceType) = UPPER(:resourceType)) AND " +
           "(:targetDept IS NULL OR :targetDept = '' OR r.targetDepartment IS NULL OR r.targetDepartment = '' OR LOWER(r.targetDepartment) = LOWER(:targetDept)) AND " +
           "(:targetCourse IS NULL OR :targetCourse = '' OR r.targetCourse IS NULL OR r.targetCourse = '' OR LOWER(r.targetCourse) = LOWER(:targetCourse)) AND " +
           "(:targetYear IS NULL OR :targetYear = 0 OR r.targetYear IS NULL OR r.targetYear = 0 OR r.targetYear = :targetYear) AND " +
           "(:targetSem IS NULL OR :targetSem = 0 OR r.targetSemester IS NULL OR r.targetSemester = 0 OR r.targetSemester = :targetSem) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.subject) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY r.id DESC")
    List<AcademicResource> filterResourcesForStudent(
            @Param("category") String category,
            @Param("subject") String subject,
            @Param("resourceType") String resourceType,
            @Param("targetDept") String targetDept,
            @Param("targetCourse") String targetCourse,
            @Param("targetYear") Integer targetYear,
            @Param("targetSem") Integer targetSem,
            @Param("search") String search
    );

    @Query("SELECT r FROM AcademicResource r WHERE " +
           "r.published = true AND r.active = true AND " +
           "(:category IS NULL OR :category = '' OR LOWER(r.category) = LOWER(:category)) AND " +
           "(:subject IS NULL OR :subject = '' OR LOWER(r.subject) = LOWER(:subject)) AND " +
           "(:resourceType IS NULL OR :resourceType = '' OR UPPER(r.resourceType) = UPPER(:resourceType)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.subject) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY r.id DESC")
    List<AcademicResource> filterResources(
            @Param("category") String category,
            @Param("subject") String subject,
            @Param("resourceType") String resourceType,
            @Param("search") String search
    );
}
