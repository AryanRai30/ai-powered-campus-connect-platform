package com.campusconnect.repository;

import com.campusconnect.entity.OpportunityApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for OpportunityApplication entities.
 */
@Repository
public interface OpportunityApplicationRepository extends JpaRepository<OpportunityApplication, Long> {

    boolean existsByOpportunityIdAndUserId(Long opportunityId, Long userId);

    Optional<OpportunityApplication> findByOpportunityIdAndUserId(Long opportunityId, Long userId);

    List<OpportunityApplication> findByUserIdOrderByAppliedAtDesc(Long userId);

    List<OpportunityApplication> findByOpportunityId(Long opportunityId);

    long countByOpportunityId(Long opportunityId);
}
