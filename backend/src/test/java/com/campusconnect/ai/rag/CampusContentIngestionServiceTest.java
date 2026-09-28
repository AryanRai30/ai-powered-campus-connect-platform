package com.campusconnect.ai.rag;

import com.campusconnect.ai.rag.dto.DocumentIngestionResult;
import com.campusconnect.ai.rag.dto.IngestionSummaryDto;
import com.campusconnect.ai.rag.mapper.CampusDocumentConverter;
import com.campusconnect.ai.rag.service.*;
import com.campusconnect.entity.*;
import com.campusconnect.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampusContentIngestionServiceTest {

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

    private CampusContentIngestionService ingestionSyncService;
    private DocumentIngestionService documentIngestionService;

    private Announcement publishedAnnouncement;
    private Event publishedEvent;
    private AcademicResource publishedResource;
    private Club publishedClub;
    private Opportunity publishedOpportunity;

    @BeforeEach
    void setUp() {
        CampusDocumentConverter converter = new CampusDocumentConverter();
        DocumentChunkingService chunkingService = new DocumentChunkingService();
        EmbeddingService embeddingService = new EmbeddingService();
        VectorStoreService vectorStoreService = mock(VectorStoreService.class);
        lenient().when(vectorStoreService.isStoreAvailable()).thenReturn(false);

        documentIngestionService = new DocumentIngestionService(chunkingService, embeddingService, vectorStoreService);

        ingestionSyncService = new CampusContentIngestionService(
                announcementRepository,
                eventRepository,
                academicResourceRepository,
                clubRepository,
                opportunityRepository,
                converter,
                documentIngestionService
        );

        User author = User.builder().id(1L).email("author@campus.edu").firstName("Jane").lastName("Doe").build();

        publishedAnnouncement = Announcement.builder()
                .id(101L).title("Exam Time Table").content("Schedules posted.").published(true).active(true).createdBy(author).build();
        publishedEvent = Event.builder()
                .id(201L).title("Tech Fest 2026").description("Annual fest.").venue("Campus Ground").eventDate(LocalDate.now()).published(true).active(true).createdBy(author).build();
        publishedResource = AcademicResource.builder()
                .id(301L).title("Math Syllabus").description("Course syllabus.").subject("Math").published(true).active(true).createdBy(author).build();
        publishedClub = Club.builder()
                .id(401L).name("Robotics Club").description("Robotics enthusiasts.").published(true).active(true).createdBy(author).build();
        publishedOpportunity = Opportunity.builder()
                .id(501L).title("Software Engineer Intern").organization("Tech Corp").description("Summer internship.").published(true).active(true).createdBy(author).build();
    }

    @Test
    @DisplayName("Should sync all published campus data sources successfully")
    void testSyncAllCampusContent() {
        when(announcementRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of(publishedAnnouncement));
        when(eventRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of(publishedEvent));
        when(academicResourceRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of(publishedResource));
        when(clubRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of(publishedClub));
        when(opportunityRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of(publishedOpportunity));

        IngestionSummaryDto summary = ingestionSyncService.syncAllCampusContent();

        assertNotNull(summary);
        assertTrue(summary.isSuccess());
        assertEquals(1, summary.getAnnouncementsProcessed());
        assertEquals(1, summary.getEventsProcessed());
        assertEquals(1, summary.getAcademicResourcesProcessed());
        assertEquals(1, summary.getClubsProcessed());
        assertEquals(1, summary.getOpportunitiesProcessed());
        assertEquals(5, summary.getTotalDocumentsProcessed());
    }

    @Test
    @DisplayName("Should be idempotent when sync is executed repeatedly for the same records")
    void testIdempotentSync() {
        when(announcementRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of(publishedAnnouncement));
        when(eventRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of(publishedEvent));
        when(academicResourceRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of(publishedResource));
        when(clubRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of(publishedClub));
        when(opportunityRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of(publishedOpportunity));

        IngestionSummaryDto firstRun = ingestionSyncService.syncAllCampusContent();
        IngestionSummaryDto secondRun = ingestionSyncService.syncAllCampusContent();

        assertEquals(firstRun.getTotalDocumentsProcessed(), secondRun.getTotalDocumentsProcessed());
        assertEquals(5, secondRun.getTotalDocumentsProcessed());
    }

    @Test
    @DisplayName("Should skip single announcement sync if draft or missing")
    void testSyncSingleAnnouncementDraftOrMissing() {
        when(announcementRepository.findById(999L)).thenReturn(Optional.empty());
        DocumentIngestionResult resultMissing = ingestionSyncService.syncSingleAnnouncement(999L);
        assertFalse(resultMissing.isSuccess());

        Announcement draft = Announcement.builder().id(102L).title("Draft Announcement").content("Draft").published(false).active(true).build();
        when(announcementRepository.findById(102L)).thenReturn(Optional.of(draft));

        DocumentIngestionResult resultDraft = ingestionSyncService.syncSingleAnnouncement(102L);
        assertFalse(resultDraft.isSuccess());
    }

    @Test
    @DisplayName("Should continue processing remaining records even if one record throws an exception")
    void testPartialFailureResilience() {
        Announcement faultyAnnouncement = Announcement.builder().id(999L).published(true).active(true).build(); // Title/content null
        when(announcementRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of(publishedAnnouncement, faultyAnnouncement));
        when(eventRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of(publishedEvent));
        when(academicResourceRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of());
        when(clubRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of());
        when(opportunityRepository.findByPublishedTrueAndActiveTrue()).thenReturn(List.of());

        IngestionSummaryDto summary = ingestionSyncService.syncAllCampusContent();

        assertNotNull(summary);
        assertEquals(1, summary.getAnnouncementsProcessed());
        assertEquals(1, summary.getEventsProcessed());
        assertEquals(2, summary.getTotalDocumentsProcessed());
        assertEquals(1, summary.getFailedRecords());
    }
}
