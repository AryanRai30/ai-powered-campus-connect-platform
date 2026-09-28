package com.campusconnect.ai.rag.service;

import com.campusconnect.ai.rag.dto.VectorSearchResult;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for executing vector similarity searches against the Qdrant campus knowledge base.
 */
@Service
public class SimilarityRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(SimilarityRetrievalService.class);

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    @Autowired
    public SimilarityRetrievalService(EmbeddingService embeddingService, VectorStoreService vectorStoreService) {
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
    }

    /**
     * Retrieves top-k most similar campus knowledge segments for a user query string.
     */
    public List<VectorSearchResult> retrieveSimilarSegments(String query, int maxResults, double minScoreThreshold) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        TextSegment querySegment = TextSegment.from(query.trim());
        Embedding queryEmbedding = embeddingService.embed(querySegment);

        List<EmbeddingMatch<TextSegment>> matches = vectorStoreService.findRelevant(
                queryEmbedding, maxResults > 0 ? maxResults : 5, minScoreThreshold);

        List<VectorSearchResult> results = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> match : matches) {
            TextSegment embedded = match.embedded();
            Map<String, String> metadataMap = new HashMap<>();
            if (embedded != null && embedded.metadata() != null) {
                embedded.metadata().toMap().forEach((key, val) -> metadataMap.put(key, val != null ? String.valueOf(val) : null));
            }

            results.add(VectorSearchResult.builder()
                    .segmentId(match.embeddingId())
                    .text(embedded != null ? embedded.text() : "")
                    .score(match.score())
                    .metadata(metadataMap)
                    .build());
        }

        log.debug("Similarity retrieval for query '{}' returned {} matches.", query, results.size());
        return results;
    }
}
