package com.campusconnect.repository;

import com.campusconnect.entity.Event;
import com.campusconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Event entities.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByCreatedByOrderByIdDesc(User createdBy);

    Optional<Event> findByIdAndCreatedBy(Long id, User createdBy);

    long countByCreatedBy(User createdBy);

    @Query("SELECT e FROM Event e WHERE " +
           "e.published = true AND e.active = true AND " +
           "(:category IS NULL OR :category = '' OR LOWER(e.category) = LOWER(:category)) AND " +
           "(:targetDept IS NULL OR :targetDept = '' OR e.targetDepartment IS NULL OR e.targetDepartment = '' OR LOWER(e.targetDepartment) = LOWER(:targetDept)) AND " +
           "(:targetCourse IS NULL OR :targetCourse = '' OR e.targetCourse IS NULL OR e.targetCourse = '' OR LOWER(e.targetCourse) = LOWER(:targetCourse)) AND " +
           "(:targetYear IS NULL OR :targetYear = 0 OR e.targetYear IS NULL OR e.targetYear = 0 OR e.targetYear = :targetYear) AND " +
           "(:targetSem IS NULL OR :targetSem = 0 OR e.targetSemester IS NULL OR e.targetSemester = 0 OR e.targetSemester = :targetSem) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.venue) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.organizer) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY e.eventDate ASC, e.eventTime ASC")
    List<Event> filterEventsForStudent(
            @Param("category") String category,
            @Param("targetDept") String targetDept,
            @Param("targetCourse") String targetCourse,
            @Param("targetYear") Integer targetYear,
            @Param("targetSem") Integer targetSem,
            @Param("search") String search
    );

    @Query("SELECT e FROM Event e WHERE " +
           "e.published = true AND e.active = true AND " +
           "(:category IS NULL OR :category = '' OR LOWER(e.category) = LOWER(:category)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.venue) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.organizer) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY e.eventDate ASC, e.eventTime ASC")
    List<Event> filterEvents(@Param("category") String category, @Param("search") String search);
}
