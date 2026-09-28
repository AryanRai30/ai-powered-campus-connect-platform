package com.campusconnect.ai.search;

import com.campusconnect.ai.rag.dto.VectorSearchResult;
import com.campusconnect.ai.rag.service.EmbeddingService;
import com.campusconnect.ai.rag.service.RagRetrievalService;
import com.campusconnect.ai.rag.service.VectorStoreService;
import com.campusconnect.ai.search.dto.CampusSearchRequest;
import com.campusconnect.ai.search.dto.CampusSearchResultItem;
import com.campusconnect.ai.search.dto.CampusSearchResponse;
import com.campusconnect.ai.search.service.CampusSearchService;
import com.campusconnect.repository.StudentProfileRepository;
import com.campusconnect.repository.UserRepository;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampusSearchServiceTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private VectorStoreService vectorStoreService;

    @Mock
    private RagRetrievalService ragRetrievalService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    private CampusSearchService campusSearchService;

    @BeforeEach
    void setUp() {
        campusSearchService = new CampusSearchService(
                embeddingService,
                vectorStoreService,
                ragRetrievalService,
                userRepository,
                studentProfileRepository
        );

        lenient().when(embeddingService.embed(any())).thenReturn(Embedding.from(new float[]{0.1f, 0.2f}));
        lenient().when(ragRetrievalService.filterAuthorizedChunks(anyList(), any(), any(), any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should return ranked campus search results for programming clubs query")
    void testSearchProgrammingClubs() {
        CampusSearchRequest request = CampusSearchRequest.builder()
                .query("programming clubs")
                .build();

        Metadata metadata = Metadata.from(Map.of(
                "sourceType", "CLUB",
                "title", "Coding Club",
                "targetDepartment", "ALL"
        ));
        TextSegment segment = TextSegment.from("[Student Club]\nClub Name: Coding Club\nDescription: Official campus coding club.", metadata);
        EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.88, "CLUB-1", Embedding.from(new float[]{0.1f, 0.2f}), segment);

        when(vectorStoreService.findRelevant(any(), anyInt(), anyDouble())).thenReturn(List.of(match));

        CampusSearchResponse response = campusSearchService.searchCampusKnowledge(request, "student@campus.edu");

        assertNotNull(response);
        assertEquals("programming clubs", response.getQuery());
        assertEquals(1, response.getTotalResults());
        CampusSearchResultItem item = response.getResults().get(0);
        assertEquals("Coding Club", item.getTitle());
        assertEquals("CLUB", item.getSourceType());
        assertTrue(item.getScore() > 0.50);
    }

    @Test
    @DisplayName("Should return empty search response for blank query")
    void testSearchBlankQuery() {
        CampusSearchRequest request = CampusSearchRequest.builder().query("  ").build();

        CampusSearchResponse response = campusSearchService.searchCampusKnowledge(request, "student@campus.edu");

        assertNotNull(response);
        assertEquals(0, response.getTotalResults());
        assertTrue(response.getResults().isEmpty());
    }

    @Test
    @DisplayName("Should fall back to MySQL retrieval when Qdrant yields 0 matches")
    void testSearchFallbackToMySql() {
        CampusSearchRequest request = CampusSearchRequest.builder()
                .query("upcoming campus events")
                .build();

        when(vectorStoreService.findRelevant(any(), anyInt(), anyDouble())).thenReturn(List.of());

        VectorSearchResult fallbackResult = VectorSearchResult.builder()
                .segmentId("EVENT-10")
                .text("[Campus Event]\nTitle: Annual Tech Hackathon 2026")
                .score(1.0)
                .metadata(Map.of("sourceType", "EVENT", "title", "Annual Tech Hackathon 2026"))
                .build();

        when(ragRetrievalService.performFallbackRetrieval(eq("upcoming campus events"), any(), any(), any(), any()))
                .thenReturn(List.of(fallbackResult));

        CampusSearchResponse response = campusSearchService.searchCampusKnowledge(request, "student@campus.edu");

        assertNotNull(response);
        assertEquals(1, response.getTotalResults());
        assertEquals("Annual Tech Hackathon 2026", response.getResults().get(0).getTitle());
        assertEquals("EVENT", response.getResults().get(0).getSourceType());
    }

    @Test
    @DisplayName("Should return zero results for unrelated general queries")
    void testSearchUnrelatedQueriesReturnZeroResults() {
        List<String> unrelatedQueries = List.of(
                "What are the best restaurants in Delhi?",
                "Explain quantum computing algorithms",
                "What is the capital of Australia?",
                "Tell me about SpaceX missions",
                "What are the symptoms of diabetes?",
                "How does cryptocurrency mining work?",
                "What is the latest iPhone model?",
                "Tell me about the history of the Roman Empire"
        );

        for (String q : unrelatedQueries) {
            CampusSearchRequest request = CampusSearchRequest.builder()
                    .query(q)
                    .build();

            CampusSearchResponse response = campusSearchService.searchCampusKnowledge(request, "student@campus.edu");

            assertNotNull(response, "Response should not be null for query: " + q);
            assertEquals(0, response.getTotalResults(), "Total results should be 0 for unrelated query: " + q);
            assertTrue(response.getResults().isEmpty(), "Results list should be empty for unrelated query: " + q);
        }

        // Verify vector store and fallback retrieval were never called for unrelated queries
        verifyNoInteractions(vectorStoreService);
        verify(ragRetrievalService, never()).performFallbackRetrieval(anyString(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should accept valid semantic campus queries")
    void testCampusDomainQueries() {
        List<String> campusQueries = List.of(
                "programming clubs",
                "communities for coding enthusiasts",
                "backend development internships",
                "materials to help me study",
                "upcoming campus events",
                "recent campus announcements"
        );

        for (String q : campusQueries) {
            assertTrue(campusSearchService.isCampusDomainQuery(q, null),
                    "Query should be recognized as campus domain: " + q);
        }
    }

    @Test
    @DisplayName("Should preserve category filter functionality even for general search text")
    void testCategoryFilterPreservesCampusIntent() {
        CampusSearchRequest request = CampusSearchRequest.builder()
                .query("tech")
                .category("CLUB")
                .build();

        when(vectorStoreService.findRelevant(any(), anyInt(), anyDouble())).thenReturn(List.of(
                new EmbeddingMatch<>(0.85, "CLUB-99", Embedding.from(new float[]{0.1f, 0.2f}),
                        TextSegment.from("[Student Club]\nClub Name: Tech Innovators", Metadata.from(Map.of("sourceType", "CLUB", "title", "Tech Innovators"))))
        ));

        CampusSearchResponse response = campusSearchService.searchCampusKnowledge(request, "student@campus.edu");

        assertNotNull(response);
        assertEquals(1, response.getTotalResults());
        assertEquals("Tech Innovators", response.getResults().get(0).getTitle());
    }

    @Test
    @DisplayName("Case A & B: Frontend internship query must NOT return backend-only internship")
    void testBackendVsFrontendInternshipRelevance() {
        Metadata metadata = Metadata.from(Map.of(
                "sourceType", "OPPORTUNITY",
                "title", "Backend Developer Internship",
                "category", "OPPORTUNITY"
        ));
        TextSegment segment = TextSegment.from("Backend Developer Internship. Requirements: Java, Spring Boot, REST APIs.", metadata);
        EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.65, "OPP-1", Embedding.from(new float[]{0.1f, 0.2f}), segment);

        when(vectorStoreService.findRelevant(any(), anyInt(), anyDouble())).thenReturn(List.of(match));

        // Case A: Query "internship for backend developer" -> Should match
        CampusSearchRequest reqA = CampusSearchRequest.builder().query("internship for backend developer").build();
        CampusSearchResponse respA = campusSearchService.searchCampusKnowledge(reqA, "student@campus.edu");
        assertEquals(1, respA.getTotalResults(), "Case A should return backend internship");

        // Case B: Query "internship for frontend developer" -> Must NOT return backend-only internship
        CampusSearchRequest reqB = CampusSearchRequest.builder().query("internship for frontend developer").build();
        CampusSearchResponse respB = campusSearchService.searchCampusKnowledge(reqB, "student@campus.edu");
        assertEquals(0, respB.getTotalResults(), "Case B MUST NOT return backend-only internship for frontend query");
    }

    @Test
    @DisplayName("Case C & D: Frontend development opportunities query must NOT return backend opportunity")
    void testBackendVsFrontendOpportunitiesRelevance() {
        Metadata metadata = Metadata.from(Map.of(
                "sourceType", "OPPORTUNITY",
                "title", "Backend Developer Internship"
        ));
        TextSegment segment = TextSegment.from("Backend Developer Internship", metadata);
        EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.60, "OPP-1", Embedding.from(new float[]{0.1f, 0.2f}), segment);

        when(vectorStoreService.findRelevant(any(), anyInt(), anyDouble())).thenReturn(List.of(match));

        // Case C: "backend development opportunities"
        CampusSearchResponse respC = campusSearchService.searchCampusKnowledge(
                CampusSearchRequest.builder().query("backend development opportunities").build(), "student@campus.edu");
        assertEquals(1, respC.getTotalResults());

        // Case D: "frontend development opportunities" -> 0 results
        CampusSearchResponse respD = campusSearchService.searchCampusKnowledge(
                CampusSearchRequest.builder().query("frontend development opportunities").build(), "student@campus.edu");
        assertEquals(0, respD.getTotalResults());
    }

    @Test
    @DisplayName("Case E & F: Sports club query must NOT return coding club")
    void testSportsVsCodingClubRelevance() {
        Metadata metadata = Metadata.from(Map.of(
                "sourceType", "CLUB",
                "title", "Coding & Competitive Programming Club"
        ));
        TextSegment segment = TextSegment.from("Official campus coding club.", metadata);
        EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.58, "CLUB-1", Embedding.from(new float[]{0.1f, 0.2f}), segment);

        when(vectorStoreService.findRelevant(any(), anyInt(), anyDouble())).thenReturn(List.of(match));

        // Case E: "AI programming clubs" -> matches
        CampusSearchResponse respE = campusSearchService.searchCampusKnowledge(
                CampusSearchRequest.builder().query("AI programming clubs").build(), "student@campus.edu");
        assertEquals(1, respE.getTotalResults());

        // Case F: "sports clubs" -> 0 results (must not return Coding Club)
        CampusSearchResponse respF = campusSearchService.searchCampusKnowledge(
                CampusSearchRequest.builder().query("sports clubs").build(), "student@campus.edu");
        assertEquals(0, respF.getTotalResults());
    }

    @Test
    @DisplayName("Case G & H: Python study resources query must NOT return Java resources")
    void testJavaVsPythonResourcesRelevance() {
        Metadata metadata = Metadata.from(Map.of(
                "sourceType", "RESOURCE",
                "title", "Java Programming Masterclass Notes"
        ));
        TextSegment segment = TextSegment.from("Complete Java programming notes.", metadata);
        EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.60, "RES-1", Embedding.from(new float[]{0.1f, 0.2f}), segment);

        when(vectorStoreService.findRelevant(any(), anyInt(), anyDouble())).thenReturn(List.of(match));

        // Case G: "Java study resources" -> matches
        CampusSearchResponse respG = campusSearchService.searchCampusKnowledge(
                CampusSearchRequest.builder().query("Java study resources").build(), "student@campus.edu");
        assertEquals(1, respG.getTotalResults());

        // Case H: "Python study resources" -> 0 results
        CampusSearchResponse respH = campusSearchService.searchCampusKnowledge(
                CampusSearchRequest.builder().query("Python study resources").build(), "student@campus.edu");
        assertEquals(0, respH.getTotalResults());
    }

    @Test
    @DisplayName("Case I: Quantum computing internship must return 0 results when none exists")
    void testQuantumComputingInternshipZeroResults() {
        Metadata metadata = Metadata.from(Map.of(
                "sourceType", "OPPORTUNITY",
                "title", "Backend Developer Internship"
        ));
        TextSegment segment = TextSegment.from("Backend Developer Internship.", metadata);
        EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.55, "OPP-1", Embedding.from(new float[]{0.1f, 0.2f}), segment);

        when(vectorStoreService.findRelevant(any(), anyInt(), anyDouble())).thenReturn(List.of(match));

        CampusSearchResponse respI = campusSearchService.searchCampusKnowledge(
                CampusSearchRequest.builder().query("quantum computing internship").build(), "student@campus.edu");
        assertEquals(0, respI.getTotalResults());
    }
}
