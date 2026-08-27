package com.campusconnect.service;

import com.campusconnect.dto.OpportunityApplicationStatusResponse;
import com.campusconnect.dto.OpportunityBookmarkStatusResponse;
import com.campusconnect.dto.OpportunityResponse;
import com.campusconnect.entity.Opportunity;
import com.campusconnect.entity.OpportunityApplication;
import com.campusconnect.entity.OpportunityBookmark;
import com.campusconnect.entity.User;
import com.campusconnect.exception.OpportunityAlreadyAppliedException;
import com.campusconnect.exception.OpportunityAlreadyBookmarkedException;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.OpportunityApplicationRepository;
import com.campusconnect.repository.OpportunityBookmarkRepository;
import com.campusconnect.repository.OpportunityRepository;
import com.campusconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service providing business logic for Campus Opportunities, Bookmarking, and Application Tracking.
 */
@Service
@Transactional
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final OpportunityBookmarkRepository bookmarkRepository;
    private final OpportunityApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public OpportunityService(
            OpportunityRepository opportunityRepository,
            OpportunityBookmarkRepository bookmarkRepository,
            OpportunityApplicationRepository applicationRepository,
            UserRepository userRepository
    ) {
        this.opportunityRepository = opportunityRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<OpportunityResponse> getAllOpportunities(
            String opportunityType,
            String location,
            String search,
            String currentUserEmail
    ) {
        User currentUser = currentUserEmail != null ? userRepository.findByEmail(currentUserEmail).orElse(null) : null;
        List<Opportunity> opportunities = opportunityRepository.filterOpportunities(opportunityType, location, search);

        return opportunities.stream()
                .map(opp -> mapToResponse(opp, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OpportunityResponse getOpportunityById(Long id, String currentUserEmail) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));
        User currentUser = currentUserEmail != null ? userRepository.findByEmail(currentUserEmail).orElse(null) : null;
        return mapToResponse(opportunity, currentUser);
    }

    public OpportunityBookmarkStatusResponse bookmarkOpportunity(Long opportunityId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + opportunityId));

        if (bookmarkRepository.existsByOpportunityIdAndUserId(opportunityId, user.getId())) {
            throw new OpportunityAlreadyBookmarkedException("Opportunity is already saved in your bookmarks.");
        }

        OpportunityBookmark bookmark = OpportunityBookmark.builder()
                .opportunity(opportunity)
                .user(user)
                .build();

        OpportunityBookmark saved = bookmarkRepository.save(bookmark);

        return OpportunityBookmarkStatusResponse.builder()
                .opportunityId(opportunityId)
                .bookmarked(true)
                .bookmarkedAt(saved.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public OpportunityBookmarkStatusResponse getBookmarkStatus(Long opportunityId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        if (!opportunityRepository.existsById(opportunityId)) {
            throw new ResourceNotFoundException("Opportunity not found with id: " + opportunityId);
        }

        Optional<OpportunityBookmark> bookmarkOpt = bookmarkRepository.findByOpportunityIdAndUserId(opportunityId, user.getId());

        return OpportunityBookmarkStatusResponse.builder()
                .opportunityId(opportunityId)
                .bookmarked(bookmarkOpt.isPresent())
                .bookmarkedAt(bookmarkOpt.map(OpportunityBookmark::getCreatedAt).orElse(null))
                .build();
    }

    @Transactional(readOnly = true)
    public List<OpportunityResponse> getMyBookmarks(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        List<OpportunityBookmark> bookmarks = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return bookmarks.stream()
                .map(b -> mapToResponse(b.getOpportunity(), user))
                .collect(Collectors.toList());
    }

    public OpportunityApplicationStatusResponse applyForOpportunity(Long opportunityId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + opportunityId));

        if (applicationRepository.existsByOpportunityIdAndUserId(opportunityId, user.getId())) {
            throw new OpportunityAlreadyAppliedException("You have already submitted/tracked an application for this opportunity.");
        }

        OpportunityApplication application = OpportunityApplication.builder()
                .opportunity(opportunity)
                .user(user)
                .status("APPLIED")
                .build();

        OpportunityApplication saved = applicationRepository.save(application);

        return OpportunityApplicationStatusResponse.builder()
                .opportunityId(opportunityId)
                .applied(true)
                .status(saved.getStatus())
                .appliedAt(saved.getAppliedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public OpportunityApplicationStatusResponse getApplicationStatus(Long opportunityId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        if (!opportunityRepository.existsById(opportunityId)) {
            throw new ResourceNotFoundException("Opportunity not found with id: " + opportunityId);
        }

        Optional<OpportunityApplication> appOpt = applicationRepository.findByOpportunityIdAndUserId(opportunityId, user.getId());

        return OpportunityApplicationStatusResponse.builder()
                .opportunityId(opportunityId)
                .applied(appOpt.isPresent())
                .status(appOpt.map(OpportunityApplication::getStatus).orElse(null))
                .appliedAt(appOpt.map(OpportunityApplication::getAppliedAt).orElse(null))
                .build();
    }

    @Transactional(readOnly = true)
    public List<OpportunityResponse> getMyApplications(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        List<OpportunityApplication> applications = applicationRepository.findByUserIdOrderByAppliedAtDesc(user.getId());

        return applications.stream()
                .map(a -> mapToResponse(a.getOpportunity(), user))
                .collect(Collectors.toList());
    }

    private OpportunityResponse mapToResponse(Opportunity opp, User currentUser) {
        boolean isBookmarked = false;
        boolean isApplied = false;
        String appStatus = null;

        if (currentUser != null) {
            isBookmarked = bookmarkRepository.existsByOpportunityIdAndUserId(opp.getId(), currentUser.getId());
            Optional<OpportunityApplication> appOpt = applicationRepository.findByOpportunityIdAndUserId(opp.getId(), currentUser.getId());
            if (appOpt.isPresent()) {
                isApplied = true;
                appStatus = appOpt.get().getStatus();
            }
        }

        return OpportunityResponse.builder()
                .id(opp.getId())
                .title(opp.getTitle())
                .description(opp.getDescription())
                .organization(opp.getOrganization())
                .opportunityType(opp.getOpportunityType())
                .location(opp.getLocation())
                .skills(opp.getSkills())
                .deadline(opp.getDeadline())
                .applicationUrl(opp.getApplicationUrl())
                .bookmarked(isBookmarked)
                .applied(isApplied)
                .applicationStatus(appStatus)
                .createdAt(opp.getCreatedAt())
                .updatedAt(opp.getUpdatedAt())
                .build();
    }
}
