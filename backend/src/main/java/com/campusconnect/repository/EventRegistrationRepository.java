package com.campusconnect.repository;

import com.campusconnect.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for EventRegistration entities.
 */
@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);

    long countByEventId(Long eventId);

    java.util.List<EventRegistration> findByEventId(Long eventId);

    java.util.List<EventRegistration> findByUserIdOrderByRegisteredAtDesc(Long userId);
}
