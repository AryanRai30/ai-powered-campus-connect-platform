package com.campusconnect.repository;

import com.campusconnect.entity.Opportunity;
import com.campusconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Opportunity entities.
 */
@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    List<Opportunity> findAllByCreatedByOrderByIdDesc(User createdBy);

    Optional<Opportunity> findByIdAndCreatedBy(Long id, User createdBy);

    long countByCreatedBy(User createdBy);

    @Query("SELECT o FROM Opportunity o WHERE " +
           "o.published = true AND o.active = true AND " +
           "(:opportunityType IS NULL OR :opportunityType = '' OR UPPER(o.opportunityType) = UPPER(:opportunityType)) AND " +
           "(:location IS NULL OR :location = '' OR LOWER(o.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:targetDept IS NULL OR :targetDept = '' OR o.targetDepartment IS NULL OR o.targetDepartment = '' OR LOWER(o.targetDepartment) = LOWER(:targetDept)) AND " +
           "(:targetCourse IS NULL OR :targetCourse = '' OR o.targetCourse IS NULL OR o.targetCourse = '' OR LOWER(o.targetCourse) = LOWER(:targetCourse)) AND " +
           "(:targetYear IS NULL OR :targetYear = 0 OR o.targetYear IS NULL OR o.targetYear = 0 OR o.targetYear = :targetYear) AND " +
           "(:targetSem IS NULL OR :targetSem = 0 OR o.targetSemester IS NULL OR o.targetSemester = 0 OR o.targetSemester = :targetSem) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(o.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.organization) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.skills) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY o.createdAt DESC")
    List<Opportunity> filterOpportunitiesForStudent(
            @Param("opportunityType") String opportunityType,
            @Param("location") String location,
            @Param("targetDept") String targetDept,
            @Param("targetCourse") String targetCourse,
            @Param("targetYear") Integer targetYear,
            @Param("targetSem") Integer targetSem,
            @Param("search") String search
    );

    @Query("SELECT o FROM Opportunity o WHERE " +
           "o.published = true AND o.active = true AND " +
           "(:opportunityType IS NULL OR :opportunityType = '' OR UPPER(o.opportunityType) = UPPER(:opportunityType)) AND " +
           "(:location IS NULL OR :location = '' OR LOWER(o.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(o.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.organization) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.skills) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY o.createdAt DESC")
    List<Opportunity> filterOpportunities(
            @Param("opportunityType") String opportunityType,
            @Param("location") String location,
            @Param("search") String search
    );
}
