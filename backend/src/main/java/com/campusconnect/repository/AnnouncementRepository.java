package com.campusconnect.repository;

import com.campusconnect.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for Announcement entities.
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAllByOrderByPublishedAtDesc();

    @Query("SELECT a FROM Announcement a WHERE " +
           "(:category IS NULL OR :category = '' OR LOWER(a.category) = LOWER(:category)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY a.publishedAt DESC")
    List<Announcement> filterAnnouncements(@Param("category") String category, @Param("search") String search);
}
