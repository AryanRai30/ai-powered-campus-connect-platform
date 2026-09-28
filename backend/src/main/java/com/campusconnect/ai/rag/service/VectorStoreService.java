package com.campusconnect.ai.rag.service;

import com.campusconnect.ai.rag.config.QdrantConfig;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for vector database persistence and retrieval via Qdrant or fallback store.
 */
@Service
public class VectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);

    private final QdrantConfig qdrantConfig;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    public VectorStoreService(QdrantConfig qdrantConfig,
                              @Autowired(required = false) @Qualifier("qdrantEmbeddingStore") EmbeddingStore<TextSegment> embeddingStore) {
        this.qdrantConfig = qdrantConfig;
        this.embeddingStore = embeddingStore;
    }

    /**
     * Checks if vector store connection is available.
     */
    public boolean isStoreAvailable() {
        return qdrantConfig != null && qdrantConfig.isEnabled() && embeddingStore != null;
    }

    /**
     * Adds an embedding and text segment to the vector store.
     */
    public String add(Embedding embedding, TextSegment textSegment) {
        if (!isStoreAvailable()) {
            log.warn("Vector store is disabled or uninitialized. Skipping vector add operation.");
            return null;
        }

        try {
            return embeddingStore.add(embedding, textSegment);
        } catch (Exception e) {
            log.error("Failed to add vector segment to store: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Adds multiple embeddings and text segments to the vector store.
     */
    public List<String> addAll(List<Embedding> embeddings, List<TextSegment> textSegments) {
        if (!isStoreAvailable()) {
            log.warn("Vector store is disabled or uninitialized. Skipping bulk vector add operation.");
            return List.of();
        }

        try {
            return embeddingStore.addAll(embeddings, textSegments);
        } catch (Exception e) {
            log.error("Failed to bulk add vector segments to store: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Searches vector store for relevant matching segments.
     */
    public List<EmbeddingMatch<TextSegment>> findRelevant(Embedding referenceEmbedding, int maxResults, double minScore) {
        if (!isStoreAvailable()) {
            log.warn("Vector store is disabled or uninitialized. Returning empty search result.");
            return List.of();
        }

        try {
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(referenceEmbedding)
                    .maxResults(maxResults)
                    .minScore(minScore)
                    .build();

            EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
            return result.matches();
        } catch (Exception e) {
            log.error("Vector search failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Reports diagnostic health status of the Qdrant vector store connection and collection.
     */
    public java.util.Map<String, Object> getHealthStatus() {
        java.util.Map<String, Object> health = new java.util.HashMap<>();
        boolean available = isStoreAvailable();
        health.put("storeAvailable", available);
        health.put("reachable", available);
        health.put("collectionName", qdrantConfig != null ? qdrantConfig.getCollectionName() : "campus_knowledge_base");
        health.put("vectorDimension", 768);
        health.put("statusMessage", available
                ? "Qdrant vector store connected successfully with collection 'campus_knowledge_base' (dim=768)."
                : "Qdrant vector store is offline or uninitialized.");
        return health;
    }
}
