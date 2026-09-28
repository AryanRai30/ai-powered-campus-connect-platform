package com.campusconnect.ai.rag.service;

import com.campusconnect.ai.rag.dto.IngestionSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener component that triggers automatic campus knowledge base sync
 * once upon Spring Boot ApplicationReadyEvent.
 * Executes asynchronously in a background thread so application startup is never blocked.
 */
@Component
public class CampusKnowledgeStartupIngestor {

    private static final Logger log = LoggerFactory.getLogger(CampusKnowledgeStartupIngestor.class);

    private final CampusContentIngestionService ingestionService;

    @Autowired
    public CampusKnowledgeStartupIngestor(CampusContentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application ready event received. Triggering background campus knowledge base ingestion...");
        Thread thread = new Thread(this::runIngestion, "rag-startup-ingestor");
        thread.setDaemon(true);
        thread.start();
    }

    public IngestionSummaryDto runIngestion() {
        try {
            log.info("Starting background campus knowledge base startup ingestion...");
            IngestionSummaryDto summary = ingestionService.syncAllCampusContent();
            log.info("Startup campus knowledge base ingestion completed successfully: {}", summary.getMessage());
            return summary;
        } catch (Exception e) {
            log.error("Startup campus knowledge base ingestion failed: {}", e.getMessage(), e);
            return IngestionSummaryDto.builder()
                    .success(false)
                    .message("Startup ingestion error: " + e.getMessage())
                    .build();
        }
    }
}
