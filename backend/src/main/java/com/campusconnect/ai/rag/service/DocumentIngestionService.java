package com.campusconnect.ai.rag.service;

import com.campusconnect.ai.rag.dto.DocumentIngestionRequest;
import com.campusconnect.ai.rag.dto.DocumentIngestionResult;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for parsing, chunking, embedding, and ingesting documents into the Qdrant vector database.
 */
@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final DocumentChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    @Autowired
    public DocumentIngestionService(DocumentChunkingService chunkingService,
                                    EmbeddingService embeddingService,
                                    VectorStoreService vectorStoreService) {
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
    }

    /**
     * Ingests a document by chunking text, generating embeddings, and storing them in vector store.
     */
    public DocumentIngestionResult ingestDocument(DocumentIngestionRequest request) {
        if (request == null || request.getContent() == null || request.getContent().trim().isEmpty()) {
            return DocumentIngestionResult.builder()
                    .documentId(request != null ? request.getDocumentId() : null)
                    .chunksIngested(0)
                    .success(false)
                    .message("Ingestion failed: empty or null document content.")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        String docId = request.getDocumentId() != null ? request.getDocumentId() : "doc-" + System.currentTimeMillis();
        Map<String, String> metadata = request.getMetadata() != null ? new HashMap<>(request.getMetadata()) : new HashMap<>();
        metadata.put("documentId", docId);
        if (request.getTitle() != null) metadata.put("title", request.getTitle());
        if (request.getCategory() != null) metadata.put("category", request.getCategory());
        if (request.getSourceUrl() != null) metadata.put("sourceUrl", request.getSourceUrl());

        List<TextSegment> segments = chunkingService.splitText(request.getContent(), metadata, 500, 50);
        if (segments.isEmpty()) {
            return DocumentIngestionResult.builder()
                    .documentId(docId)
                    .chunksIngested(0)
                    .success(false)
                    .message("No text segments could be created from document.")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        List<Embedding> embeddings = embeddingService.embedAll(segments);

        boolean storedInVectorDb = false;
        if (vectorStoreService.isStoreAvailable()) {
            List<String> ids = vectorStoreService.addAll(embeddings, segments);
            storedInVectorDb = !ids.isEmpty();
        }

        log.info("Ingested document '{}' with {} chunks (Vector store active: {})",
                docId, segments.size(), vectorStoreService.isStoreAvailable());

        return DocumentIngestionResult.builder()
                .documentId(docId)
                .chunksIngested(segments.size())
                .success(true)
                .message(vectorStoreService.isStoreAvailable()
                        ? "Successfully ingested document into vector store."
                        : "Processed document chunks and embeddings successfully (vector store offline).")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
