package com.campusconnect.repository;

import com.campusconnect.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for Event entities.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByOrderByEventDateAsc();

    @Query("SELECT e FROM Event e WHERE " +
           "(:category IS NULL OR :category = '' OR LOWER(e.category) = LOWER(:category)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.venue) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.organizer) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY e.eventDate ASC, e.eventTime ASC")
    List<Event> filterEvents(@Param("category") String category, @Param("search") String search);
}
