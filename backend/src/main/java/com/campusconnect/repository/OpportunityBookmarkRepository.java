package com.campusconnect.repository;

import com.campusconnect.entity.OpportunityBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for OpportunityBookmark entities.
 */
@Repository
public interface OpportunityBookmarkRepository extends JpaRepository<OpportunityBookmark, Long> {

    boolean existsByOpportunityIdAndUserId(Long opportunityId, Long userId);

    Optional<OpportunityBookmark> findByOpportunityIdAndUserId(Long opportunityId, Long userId);

    List<OpportunityBookmark> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<OpportunityBookmark> findByOpportunityId(Long opportunityId);

    void deleteByOpportunityIdAndUserId(Long opportunityId, Long userId);
}
