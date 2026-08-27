package com.campusconnect.repository;

import com.campusconnect.entity.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for Opportunity entities.
 */
@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    @Query("SELECT o FROM Opportunity o WHERE " +
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
