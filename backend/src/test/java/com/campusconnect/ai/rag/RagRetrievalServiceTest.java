package com.campusconnect.ai.rag;

import com.campusconnect.ai.rag.dto.DocumentIngestionRequest;
import com.campusconnect.ai.rag.dto.RagQueryRequest;
import com.campusconnect.ai.rag.dto.RagQueryResponse;
import com.campusconnect.ai.rag.dto.VectorSearchResult;
import com.campusconnect.ai.rag.mapper.CampusDocumentConverter;
import com.campusconnect.ai.rag.service.EmbeddingService;
import com.campusconnect.ai.rag.service.RagContextBuilder;
import com.campusconnect.ai.rag.service.RagRetrievalService;
import com.campusconnect.ai.rag.service.VectorStoreService;
import com.campusconnect.ai.service.AiAssistantService;
import com.campusconnect.entity.Club;
import com.campusconnect.repository.*;
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
class RagRetrievalServiceTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private VectorStoreService vectorStoreService;

    @Mock
    private AiAssistantService aiAssistantService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AcademicResourceRepository academicResourceRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private CampusDocumentConverter campusDocumentConverter;

    private RagRetrievalService ragRetrievalService;
    private RagContextBuilder contextBuilder;

    @BeforeEach
    void setUp() {
        contextBuilder = new RagContextBuilder();
        ragRetrievalService = new RagRetrievalService(
                embeddingService,
                vectorStoreService,
                aiAssistantService,
                contextBuilder,
                userRepository,
                studentProfileRepository,
                announcementRepository,
                eventRepository,
                academicResourceRepository,
                clubRepository,
                opportunityRepository,
                campusDocumentConverter
        );

        lenient().when(embeddingService.embed(any())).thenReturn(Embedding.from(new float[]{0.1f, 0.2f}));
    }

    @Test
    @DisplayName("Should retrieve authorized chunks from Qdrant and generate grounded AI response")
    void testQueryCampusKnowledgeSuccess() {
        RagQueryRequest request = RagQueryRequest.builder()
                .query("When do midterm exams start?")
                .department("Computer Science")
                .course("B.Tech")
                .year(3)
                .semester(6)
                .build();

        Metadata metadata = Metadata.from(Map.of(
                "sourceType", "ANNOUNCEMENT",
                "title", "Midterm Schedule",
                "targetDepartment", "Computer Science",
                "targetYear", "3"
        ));
        TextSegment segment = TextSegment.from("Midterm exams start on October 15, 2026.", metadata);
        EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.89, "segment-101", Embedding.from(new float[]{0.1f, 0.2f}), segment);

        when(vectorStoreService.findRelevant(any(), anyInt(), anyDouble())).thenReturn(List.of(match));
        when(aiAssistantService.generateResponse(anyString())).thenReturn("Midterm exams start on October 15, 2026 for CS 3rd year students.");

        RagQueryResponse response = ragRetrievalService.queryCampusKnowledge(request, "student@campus.edu");

        assertNotNull(response);
        assertTrue(response.isGrounded());
        assertEquals(1, response.getRetrievedChunksCount());
        assertEquals("Midterm exams start on October 15, 2026 for CS 3rd year students.", response.getAnswer());
        verify(aiAssistantService, times(1)).generateResponse(anyString());
    }

    @Test
    @DisplayName("Should trigger MySQL fallback retrieval when Qdrant returns 0 matches")
    void testQueryCampusKnowledgeFallbackToMySql() {
        RagQueryRequest request = RagQueryRequest.builder()
                .query("What clubs are available?")
                .build();

        when(vectorStoreService.findRelevant(any(), anyInt(), anyDouble())).thenReturn(List.of());

        Club club = new Club();
        club.setId(1L);
        club.setName("Coding Club");
        club.setPublished(true);
        club.setActive(true);

        DocumentIngestionRequest docReq = DocumentIngestionRequest.builder()
                .documentId("CLUB-1")
                .title("Coding Club")
                .category("CLUB")
                .content("[Student Club]\nClub Name: Coding Club")
                .metadata(Map.of("sourceType", "CLUB", "targetDepartment", "ALL"))
                .build();

        when(clubRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of(club));
        when(campusDocumentConverter.convertClub(club)).thenReturn(docReq);
        when(aiAssistantService.generateResponse(anyString())).thenReturn("The available clubs include: Coding Club.");

        RagQueryResponse response = ragRetrievalService.queryCampusKnowledge(request, "student@campus.edu");

        assertNotNull(response);
        assertTrue(response.isGrounded());
        assertEquals(1, response.getRetrievedChunksCount());
        assertEquals("The available clubs include: Coding Club.", response.getAnswer());
        verify(clubRepository, times(1)).findByPublishedTrueAndActiveTrue();
        verify(aiAssistantService, times(1)).generateResponse(anyString());
    }

    @Test
    @DisplayName("Should return no-result response when both Qdrant and MySQL fallback find 0 matches")
    void testQueryCampusKnowledgeNoMatches() {
        RagQueryRequest request = RagQueryRequest.builder()
                .query("What is the swimming pool schedule?")
                .build();

        when(vectorStoreService.findRelevant(any(), anyInt(), anyDouble())).thenReturn(List.of());

        RagQueryResponse response = ragRetrievalService.queryCampusKnowledge(request, "student@campus.edu");

        assertNotNull(response);
        assertFalse(response.isGrounded());
        assertEquals(0, response.getRetrievedChunksCount());
        assertEquals(RagContextBuilder.NO_INFO_FOUND_MESSAGE, response.getAnswer());
        verify(aiAssistantService, never()).generateResponse(anyString());
    }

    @Test
    @DisplayName("Should filter out chunks not targeted to student profile and return no-result message")
    void testAuthorizationFilteringExcludesUntargetedChunks() {
        RagQueryRequest request = RagQueryRequest.builder()
                .query("What are the lab guidelines?")
                .department("Electrical Engineering") // Student is EE
                .year(1)
                .build();

        // Chunk is targeted specifically to Mechanical Engineering 4th Year
        Metadata metadata = Metadata.from(Map.of(
                "sourceType", "ANNOUNCEMENT",
                "title", "Mechanical Advanced Lab Safety",
                "targetDepartment", "Mechanical Engineering",
                "targetYear", "4"
        ));
        TextSegment segment = TextSegment.from("Advanced turbine safety manual.", metadata);
        EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.92, "segment-555", Embedding.from(new float[]{0.1f, 0.2f}), segment);

        when(vectorStoreService.findRelevant(any(), anyInt(), anyDouble())).thenReturn(List.of(match));

        RagQueryResponse response = ragRetrievalService.queryCampusKnowledge(request, "ee_student@campus.edu");

        assertNotNull(response);
        assertFalse(response.isGrounded());
        assertEquals(0, response.getRetrievedChunksCount());
        assertEquals(RagContextBuilder.NO_INFO_FOUND_MESSAGE, response.getAnswer());
        verify(aiAssistantService, never()).generateResponse(anyString());
    }

    @Test
    @DisplayName("Should evaluate chunk authorization rules correctly")
    void testIsChunkAuthorizedForStudent() {
        VectorSearchResult publicChunk = VectorSearchResult.builder()
                .segmentId("1")
                .metadata(Map.of("targetDepartment", "ALL"))
                .build();
        assertTrue(ragRetrievalService.isChunkAuthorizedForStudent(publicChunk, "Computer Science", "B.Tech", 2, 4));

        VectorSearchResult allDeptsChunk = VectorSearchResult.builder()
                .segmentId("1b")
                .metadata(Map.of("targetDepartment", "All Departments"))
                .build();
        assertTrue(ragRetrievalService.isChunkAuthorizedForStudent(allDeptsChunk, "Computer Science", "B.Tech", 2, 4));

        VectorSearchResult deptChunk = VectorSearchResult.builder()
                .segmentId("2")
                .metadata(Map.of("targetDepartment", "Computer Science", "targetYear", "3"))
                .build();
        assertTrue(ragRetrievalService.isChunkAuthorizedForStudent(deptChunk, "Computer Science", "B.Tech", 3, 6));
        assertFalse(ragRetrievalService.isChunkAuthorizedForStudent(deptChunk, "Electrical Engineering", "B.Tech", 3, 6));
        assertFalse(ragRetrievalService.isChunkAuthorizedForStudent(deptChunk, "Computer Science", "B.Tech", 2, 4));
    }

    @Test
    @DisplayName("Should route general technical queries to normal LLM without returning RAG no-info message")
    void testGeneralQueryRoutingInRagService() {
        RagQueryRequest request = RagQueryRequest.builder()
                .query("What is Java?")
                .build();

        when(aiAssistantService.generateResponse("What is Java?")).thenReturn("Java is a high-level, class-based, object-oriented programming language.");

        RagQueryResponse response = ragRetrievalService.queryCampusKnowledge(request, "student@campus.edu");

        assertNotNull(response);
        assertFalse(response.isGrounded());
        assertEquals("Java is a high-level, class-based, object-oriented programming language.", response.getAnswer());
        verify(vectorStoreService, never()).findRelevant(any(), anyInt(), anyDouble());
    }

    @Test
    @DisplayName("Should correctly classify campus queries vs general queries")
    void testIsCampusQueryClassification() {
        assertTrue(ragRetrievalService.isCampusQuery("What clubs are available on campus?"));
        assertTrue(ragRetrievalService.isCampusQuery("What academic resources are available?"));
        assertTrue(ragRetrievalService.isCampusQuery("What internship opportunities are currently available?"));
        assertTrue(ragRetrievalService.isCampusQuery("Tell me about the Salesforce opportunity."));

        assertFalse(ragRetrievalService.isCampusQuery("What is Java?"));
        assertFalse(ragRetrievalService.isCampusQuery("What is Spring Boot?"));
        assertFalse(ragRetrievalService.isCampusQuery("Explain JWT authentication."));
        assertFalse(ragRetrievalService.isCampusQuery("What is React?"));
        assertFalse(ragRetrievalService.isCampusQuery("Explain microservices."));
    }
}


