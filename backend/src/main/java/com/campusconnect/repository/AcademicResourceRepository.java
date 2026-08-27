package com.campusconnect.repository;

import com.campusconnect.entity.AcademicResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for AcademicResource entities.
 */
@Repository
public interface AcademicResourceRepository extends JpaRepository<AcademicResource, Long> {

    @Query("SELECT r FROM AcademicResource r WHERE " +
           "(:category IS NULL OR :category = '' OR LOWER(r.category) = LOWER(:category)) AND " +
           "(:subject IS NULL OR :subject = '' OR LOWER(r.subject) = LOWER(:subject)) AND " +
           "(:resourceType IS NULL OR :resourceType = '' OR UPPER(r.resourceType) = UPPER(:resourceType)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.subject) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY r.title ASC")
    List<AcademicResource> filterResources(
            @Param("category") String category,
            @Param("subject") String subject,
            @Param("resourceType") String resourceType,
            @Param("search") String search
    );
}
