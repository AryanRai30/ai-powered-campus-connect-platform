package com.campusconnect.ai.rag.service;

import com.campusconnect.ai.rag.dto.DocumentIngestionRequest;
import com.campusconnect.ai.rag.dto.DocumentIngestionResult;
import com.campusconnect.ai.rag.dto.IngestionSummaryDto;
import com.campusconnect.ai.rag.mapper.CampusDocumentConverter;
import com.campusconnect.entity.*;
import com.campusconnect.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for synchronizing and re-indexing real campus content (Announcements, Events,
 * Academic Resources, Clubs, Opportunities) into the RAG vector knowledge base.
 */
@Service
public class CampusContentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(CampusContentIngestionService.class);

    private final AnnouncementRepository announcementRepository;
    private final EventRepository eventRepository;
    private final AcademicResourceRepository academicResourceRepository;
    private final ClubRepository clubRepository;
    private final OpportunityRepository opportunityRepository;
    private final CampusDocumentConverter documentConverter;
    private final DocumentIngestionService documentIngestionService;

    @Autowired
    public CampusContentIngestionService(AnnouncementRepository announcementRepository,
                                         EventRepository eventRepository,
                                         AcademicResourceRepository academicResourceRepository,
                                         ClubRepository clubRepository,
                                         OpportunityRepository opportunityRepository,
                                         CampusDocumentConverter documentConverter,
                                         DocumentIngestionService documentIngestionService) {
        this.announcementRepository = announcementRepository;
        this.eventRepository = eventRepository;
        this.academicResourceRepository = academicResourceRepository;
        this.clubRepository = clubRepository;
        this.opportunityRepository = opportunityRepository;
        this.documentConverter = documentConverter;
        this.documentIngestionService = documentIngestionService;
    }

    /**
     * Synchronizes and re-indexes all authorized published & active campus records.
     */
    /**
     * Synchronizes and re-indexes all authorized published & active campus records.
     */
    @Transactional(readOnly = true)
    public IngestionSummaryDto syncAllCampusContent() {
        log.info("Starting complete campus knowledge base synchronization...");

        java.util.concurrent.atomic.AtomicInteger totalChunks = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger totalFailed = new java.util.concurrent.atomic.AtomicInteger(0);

        int announcements = syncAnnouncements(totalChunks, totalFailed);
        int events = syncEvents(totalChunks, totalFailed);
        int resources = syncAcademicResources(totalChunks, totalFailed);
        int clubs = syncClubs(totalChunks, totalFailed);
        int opportunities = syncOpportunities(totalChunks, totalFailed);

        int totalDocs = announcements + events + resources + clubs + opportunities;
        int failedCount = totalFailed.get();
        int chunksCount = totalChunks.get();

        log.info("Campus knowledge base sync completed. Successful records: {}, Total chunks: {}, Failed records: {}",
                totalDocs, chunksCount, failedCount);

        String message = String.format("Campus knowledge base sync completed: %d records processed (%d chunks), %d failed.",
                totalDocs, chunksCount, failedCount);

        return IngestionSummaryDto.builder()
                .announcementsProcessed(announcements)
                .eventsProcessed(events)
                .academicResourcesProcessed(resources)
                .clubsProcessed(clubs)
                .opportunitiesProcessed(opportunities)
                .totalDocumentsProcessed(totalDocs)
                .totalChunksIngested(chunksCount)
                .failedRecords(failedCount)
                .success(failedCount == 0 || totalDocs > 0)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Indexes published & active Announcements.
     */
    @Transactional(readOnly = true)
    public int syncAnnouncements() {
        return syncAnnouncements(null, null);
    }

    private int syncAnnouncements(java.util.concurrent.atomic.AtomicInteger chunksAcc, java.util.concurrent.atomic.AtomicInteger failedAcc) {
        List<Announcement> list = announcementRepository.findByPublishedTrueAndActiveTrue();
        int count = 0;
        for (Announcement a : list) {
            try {
                DocumentIngestionRequest request = documentConverter.convertAnnouncement(a);
                if (request != null) {
                    DocumentIngestionResult result = documentIngestionService.ingestDocument(request);
                    if (result.isSuccess()) {
                        count++;
                        if (chunksAcc != null) chunksAcc.addAndGet(result.getChunksIngested());
                    } else {
                        if (failedAcc != null) failedAcc.incrementAndGet();
                        log.warn("Failed to ingest Announcement ID {}: {}", a.getId(), result.getMessage());
                    }
                }
            } catch (Exception e) {
                if (failedAcc != null) failedAcc.incrementAndGet();
                log.error("Exception ingesting Announcement ID {}: {}", a.getId(), e.getMessage(), e);
            }
        }
        log.debug("Ingested {} published announcements", count);
        return count;
    }

    /**
     * Indexes published & active Events.
     */
    @Transactional(readOnly = true)
    public int syncEvents() {
        return syncEvents(null, null);
    }

    private int syncEvents(java.util.concurrent.atomic.AtomicInteger chunksAcc, java.util.concurrent.atomic.AtomicInteger failedAcc) {
        List<Event> list = eventRepository.findByPublishedTrueAndActiveTrue();
        int count = 0;
        for (Event e : list) {
            try {
                DocumentIngestionRequest request = documentConverter.convertEvent(e);
                if (request != null) {
                    DocumentIngestionResult result = documentIngestionService.ingestDocument(request);
                    if (result.isSuccess()) {
                        count++;
                        if (chunksAcc != null) chunksAcc.addAndGet(result.getChunksIngested());
                    } else {
                        if (failedAcc != null) failedAcc.incrementAndGet();
                        log.warn("Failed to ingest Event ID {}: {}", e.getId(), result.getMessage());
                    }
                }
            } catch (Exception ex) {
                if (failedAcc != null) failedAcc.incrementAndGet();
                log.error("Exception ingesting Event ID {}: {}", e.getId(), ex.getMessage(), ex);
            }
        }
        log.debug("Ingested {} published events", count);
        return count;
    }

    /**
     * Indexes published & active Academic Resources.
     */
    @Transactional(readOnly = true)
    public int syncAcademicResources() {
        return syncAcademicResources(null, null);
    }

    private int syncAcademicResources(java.util.concurrent.atomic.AtomicInteger chunksAcc, java.util.concurrent.atomic.AtomicInteger failedAcc) {
        List<AcademicResource> list = academicResourceRepository.findByPublishedTrueAndActiveTrue();
        int count = 0;
        for (AcademicResource r : list) {
            try {
                DocumentIngestionRequest request = documentConverter.convertAcademicResource(r);
                if (request != null) {
                    DocumentIngestionResult result = documentIngestionService.ingestDocument(request);
                    if (result.isSuccess()) {
                        count++;
                        if (chunksAcc != null) chunksAcc.addAndGet(result.getChunksIngested());
                    } else {
                        if (failedAcc != null) failedAcc.incrementAndGet();
                        log.warn("Failed to ingest AcademicResource ID {}: {}", r.getId(), result.getMessage());
                    }
                }
            } catch (Exception ex) {
                if (failedAcc != null) failedAcc.incrementAndGet();
                log.error("Exception ingesting AcademicResource ID {}: {}", r.getId(), ex.getMessage(), ex);
            }
        }
        log.debug("Ingested {} published academic resources", count);
        return count;
    }

    /**
     * Indexes published & active Clubs.
     */
    @Transactional(readOnly = true)
    public int syncClubs() {
        return syncClubs(null, null);
    }

    private int syncClubs(java.util.concurrent.atomic.AtomicInteger chunksAcc, java.util.concurrent.atomic.AtomicInteger failedAcc) {
        List<Club> list = clubRepository.findByPublishedTrueAndActiveTrue();
        int count = 0;
        for (Club c : list) {
            try {
                DocumentIngestionRequest request = documentConverter.convertClub(c);
                if (request != null) {
                    DocumentIngestionResult result = documentIngestionService.ingestDocument(request);
                    if (result.isSuccess()) {
                        count++;
                        if (chunksAcc != null) chunksAcc.addAndGet(result.getChunksIngested());
                    } else {
                        if (failedAcc != null) failedAcc.incrementAndGet();
                        log.warn("Failed to ingest Club ID {}: {}", c.getId(), result.getMessage());
                    }
                }
            } catch (Exception ex) {
                if (failedAcc != null) failedAcc.incrementAndGet();
                log.error("Exception ingesting Club ID {}: {}", c.getId(), ex.getMessage(), ex);
            }
        }
        log.debug("Ingested {} published clubs", count);
        return count;
    }

    /**
     * Indexes published & active Opportunities.
     */
    @Transactional(readOnly = true)
    public int syncOpportunities() {
        return syncOpportunities(null, null);
    }

    private int syncOpportunities(java.util.concurrent.atomic.AtomicInteger chunksAcc, java.util.concurrent.atomic.AtomicInteger failedAcc) {
        List<Opportunity> list = opportunityRepository.findByPublishedTrueAndActiveTrue();
        int count = 0;
        for (Opportunity o : list) {
            try {
                DocumentIngestionRequest request = documentConverter.convertOpportunity(o);
                if (request != null) {
                    DocumentIngestionResult result = documentIngestionService.ingestDocument(request);
                    if (result.isSuccess()) {
                        count++;
                        if (chunksAcc != null) chunksAcc.addAndGet(result.getChunksIngested());
                    } else {
                        if (failedAcc != null) failedAcc.incrementAndGet();
                        log.warn("Failed to ingest Opportunity ID {}: {}", o.getId(), result.getMessage());
                    }
                }
            } catch (Exception ex) {
                if (failedAcc != null) failedAcc.incrementAndGet();
                log.error("Exception ingesting Opportunity ID {}: {}", o.getId(), ex.getMessage(), ex);
            }
        }
        log.debug("Ingested {} published opportunities", count);
        return count;
    }

    /**
     * Syncs a single Announcement by ID.
     */
    @Transactional(readOnly = true)
    public DocumentIngestionResult syncSingleAnnouncement(Long id) {
        Announcement a = announcementRepository.findById(id).orElse(null);
        DocumentIngestionRequest req = documentConverter.convertAnnouncement(a);
        if (req == null) {
            return DocumentIngestionResult.builder()
                    .documentId("ANNOUNCEMENT-" + id)
                    .chunksIngested(0)
                    .success(false)
                    .message("Record not found or not authorized/published for indexing.")
                    .timestamp(LocalDateTime.now())
                    .build();
        }
        return documentIngestionService.ingestDocument(req);
    }

    /**
     * Syncs a single Event by ID.
     */
    @Transactional(readOnly = true)
    public DocumentIngestionResult syncSingleEvent(Long id) {
        Event e = eventRepository.findById(id).orElse(null);
        DocumentIngestionRequest req = documentConverter.convertEvent(e);
        if (req == null) {
            return DocumentIngestionResult.builder()
                    .documentId("EVENT-" + id)
                    .chunksIngested(0)
                    .success(false)
                    .message("Record not found or not authorized/published for indexing.")
                    .timestamp(LocalDateTime.now())
                    .build();
        }
        return documentIngestionService.ingestDocument(req);
    }
}
