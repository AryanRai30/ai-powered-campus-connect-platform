package com.campusconnect.repository;

import com.campusconnect.entity.ClubMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for ClubMembership entities.
 */
@Repository
public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Long> {

    boolean existsByClubIdAndUserId(Long clubId, Long userId);

    Optional<ClubMembership> findByClubIdAndUserId(Long clubId, Long userId);

    long countByClubId(Long clubId);

    List<ClubMembership> findByUserId(Long userId);

    List<ClubMembership> findByClubId(Long clubId);
}
