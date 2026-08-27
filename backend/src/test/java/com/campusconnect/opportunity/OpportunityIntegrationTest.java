package com.campusconnect.opportunity;

import com.campusconnect.dto.OpportunityApplicationStatusResponse;
import com.campusconnect.dto.OpportunityBookmarkStatusResponse;
import com.campusconnect.dto.OpportunityResponse;
import com.campusconnect.dto.RegisterRequest;
import com.campusconnect.entity.Opportunity;
import com.campusconnect.exception.OpportunityAlreadyAppliedException;
import com.campusconnect.exception.OpportunityAlreadyBookmarkedException;
import com.campusconnect.repository.OpportunityRepository;
import com.campusconnect.service.AuthService;
import com.campusconnect.service.OpportunityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class OpportunityIntegrationTest {

    @Autowired
    private OpportunityService opportunityService;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private AuthService authService;

    private Opportunity testOpportunity;
    private final String studentEmail = "student.opportunity.test@example.com";

    @BeforeEach
    void setUp() {
        authService.register(RegisterRequest.builder()
                .firstName("Career")
                .lastName("Student")
                .email(studentEmail)
                .password("Password@123")
                .build());

        testOpportunity = opportunityRepository.save(Opportunity.builder()
                .title("Cloud Infrastructure Intern")
                .description("Hands-on internship working with Docker, Kubernetes, and AWS")
                .organization("CloudTech Labs")
                .opportunityType("INTERNSHIP")
                .location("Remote")
                .skills("AWS, Docker, Linux")
                .deadline(LocalDate.now().plusDays(15))
                .applicationUrl("https://careers.microsoft.com/")
                .build());
    }

    @Test
    @DisplayName("Should fetch all opportunities and filter by type and location")
    void shouldFetchAllOpportunitiesAndFilterByTypeAndLocation() {
        List<OpportunityResponse> opportunities = opportunityService.getAllOpportunities(
                "INTERNSHIP",
                "Remote",
                null,
                studentEmail
        );

        assertThat(opportunities).isNotEmpty();
        assertThat(opportunities.get(0).getOpportunityType()).isEqualTo("INTERNSHIP");
        assertThat(opportunities.get(0).getLocation()).isEqualTo("Remote");
    }

    @Test
    @DisplayName("Should bookmark opportunity and prevent duplicate bookmarking")
    void shouldBookmarkOpportunityAndPreventDuplicates() {
        OpportunityBookmarkStatusResponse response = opportunityService.bookmarkOpportunity(testOpportunity.getId(), studentEmail);
        assertThat(response.isBookmarked()).isTrue();

        OpportunityBookmarkStatusResponse status = opportunityService.getBookmarkStatus(testOpportunity.getId(), studentEmail);
        assertThat(status.isBookmarked()).isTrue();

        assertThatThrownBy(() -> opportunityService.bookmarkOpportunity(testOpportunity.getId(), studentEmail))
                .isInstanceOf(OpportunityAlreadyBookmarkedException.class)
                .hasMessageContaining("already saved in your bookmarks");
    }

    @Test
    @DisplayName("Should retrieve saved bookmarks for current student")
    void shouldFetchUserMyBookmarks() {
        opportunityService.bookmarkOpportunity(testOpportunity.getId(), studentEmail);

        List<OpportunityResponse> myBookmarks = opportunityService.getMyBookmarks(studentEmail);
        assertThat(myBookmarks).hasSize(1);
        assertThat(myBookmarks.get(0).getTitle()).isEqualTo("Cloud Infrastructure Intern");
    }

    @Test
    @DisplayName("Should track application submission and prevent duplicate applications")
    void shouldTrackApplicationAndPreventDuplicateApplications() {
        OpportunityApplicationStatusResponse response = opportunityService.applyForOpportunity(testOpportunity.getId(), studentEmail);
        assertThat(response.isApplied()).isTrue();
        assertThat(response.getStatus()).isEqualTo("APPLIED");

        OpportunityApplicationStatusResponse status = opportunityService.getApplicationStatus(testOpportunity.getId(), studentEmail);
        assertThat(status.isApplied()).isTrue();

        assertThatThrownBy(() -> opportunityService.applyForOpportunity(testOpportunity.getId(), studentEmail))
                .isInstanceOf(OpportunityAlreadyAppliedException.class)
                .hasMessageContaining("already submitted/tracked an application");
    }

    @Test
    @DisplayName("Should retrieve tracked applications for current student")
    void shouldFetchUserMyApplications() {
        opportunityService.applyForOpportunity(testOpportunity.getId(), studentEmail);

        List<OpportunityResponse> myApplications = opportunityService.getMyApplications(studentEmail);
        assertThat(myApplications).hasSize(1);
        assertThat(myApplications.get(0).getApplicationStatus()).isEqualTo("APPLIED");
    }
}
