package com.campusconnect.repository;

import com.campusconnect.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Club entities.
 */
@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT c FROM Club c WHERE " +
           "(:category IS NULL OR :category = '' OR LOWER(c.category) = LOWER(:category)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.presidentName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.meetingVenue) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY c.name ASC")
    List<Club> filterClubs(@Param("category") String category, @Param("search") String search);
}
