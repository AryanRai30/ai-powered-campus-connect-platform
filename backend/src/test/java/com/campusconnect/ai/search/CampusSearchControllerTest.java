package com.campusconnect.ai.search;

import com.campusconnect.ai.search.controller.CampusSearchController;
import com.campusconnect.ai.search.dto.CampusSearchRequest;
import com.campusconnect.ai.search.dto.CampusSearchResultItem;
import com.campusconnect.ai.search.dto.CampusSearchResponse;
import com.campusconnect.ai.search.service.CampusSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CampusSearchControllerTest {

    private CampusSearchService campusSearchService;
    private CampusSearchController campusSearchController;

    @BeforeEach
    void setUp() {
        campusSearchService = mock(CampusSearchService.class);
        campusSearchController = new CampusSearchController(campusSearchService);
    }

    @Test
    @DisplayName("Should process POST /api/ai/search query request via controller")
    void testSearchCampusEndpointSuccess() {
        CampusSearchRequest request = CampusSearchRequest.builder()
                .query("backend development internships")
                .build();

        CampusSearchResultItem item = CampusSearchResultItem.builder()
                .id("OPPORTUNITY-5")
                .title("Backend Software Intern")
                .content("Build Java Spring Boot REST microservices.")
                .sourceType("OPPORTUNITY")
                .score(0.91)
                .category("INTERNSHIP")
                .metadata(Map.of("sourceType", "OPPORTUNITY", "title", "Backend Software Intern"))
                .build();

        CampusSearchResponse response = CampusSearchResponse.builder()
                .query("backend development internships")
                .totalResults(1)
                .results(List.of(item))
                .timestamp(LocalDateTime.now())
                .build();

        when(campusSearchService.searchCampusKnowledge(any(CampusSearchRequest.class), any())).thenReturn(response);

        ResponseEntity<CampusSearchResponse> responseEntity = campusSearchController.searchCampus(request);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertEquals("backend development internships", responseEntity.getBody().getQuery());
        assertEquals(1, responseEntity.getBody().getTotalResults());
        assertEquals("Backend Software Intern", responseEntity.getBody().getResults().get(0).getTitle());
        assertEquals(0.91, responseEntity.getBody().getResults().get(0).getScore());
    }
}
