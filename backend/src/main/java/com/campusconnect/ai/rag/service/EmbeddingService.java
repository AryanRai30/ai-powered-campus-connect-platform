package com.campusconnect.ai.rag.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for generating vector embeddings for text segments and retrieval queries.
 * Provides fallback deterministic vector generation when no live external EmbeddingModel is configured.
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final EmbeddingModel embeddingModel;

    @Autowired(required = false)
    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public EmbeddingService() {
        this.embeddingModel = null;
    }

    /**
     * Generates a vector embedding for a single text segment.
     */
    public Embedding embed(TextSegment segment) {
        if (segment == null || segment.text() == null || segment.text().trim().isEmpty()) {
            return generateFallbackEmbedding("", 768);
        }

        if (embeddingModel != null) {
            try {
                Response<Embedding> response = embeddingModel.embed(segment);
                return response.content();
            } catch (Exception e) {
                log.warn("EmbeddingModel call failed: {}. Using fallback embedding.", e.getMessage());
            }
        }

        log.debug("Using fallback embedding generator for segment text.");
        return generateFallbackEmbedding(segment.text(), 768);
    }

    /**
     * Generates vector embeddings for a list of text segments.
     */
    public List<Embedding> embedAll(List<TextSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return List.of();
        }

        if (embeddingModel != null) {
            try {
                Response<List<Embedding>> response = embeddingModel.embedAll(segments);
                return response.content();
            } catch (Exception e) {
                log.warn("Bulk EmbeddingModel call failed: {}. Using fallback embeddings.", e.getMessage());
            }
        }

        List<Embedding> embeddings = new ArrayList<>();
        for (TextSegment segment : segments) {
            embeddings.add(embed(segment));
        }
        return embeddings;
    }

    private static final java.util.Set<String> STOP_WORDS = java.util.Set.of(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can",
            "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from",
            "further", "had", "has", "have", "having", "he", "her", "here", "hers", "herself", "him", "himself", "his",
            "how", "i", "if", "in", "into", "is", "it", "its", "itself", "just", "me", "more", "most", "my",
            "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "our", "ours", "ourselves",
            "out", "over", "own", "same", "she", "should", "so", "some", "such", "than", "that", "the", "their",
            "theirs", "them", "themselves", "then", "there", "these", "they", "this", "those", "through", "to", "too",
            "under", "until", "up", "very", "was", "we", "were", "what", "when", "where", "which", "while",
            "who", "whom", "why", "with", "would", "you", "your", "yours", "yourself", "yourselves",
            "available", "currently", "please", "advised", "note", "take", "give", "show", "tell", "list", "get", "find"
    );

    /**
     * Normalizes a token to its stem / canonical form.
     */
    private String stemToken(String token) {
        if (token == null || token.isEmpty()) return "";
        String t = token.toLowerCase();
        if (t.endsWith("ies") && t.length() > 4) {
            return t.substring(0, t.length() - 3) + "y";
        }
        if (t.endsWith("s") && !t.endsWith("ss") && t.length() > 3) {
            return t.substring(0, t.length() - 1);
        }
        return t;
    }

    /**
     * Generates a normalized fallback embedding vector of given dimension based on token feature hashing.
     */
    public Embedding generateFallbackEmbedding(String text, int dimension) {
        float[] vector = new float[dimension];
        if (text == null || text.trim().isEmpty()) {
            return Embedding.from(vector);
        }

        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] rawTokens = cleaned.split("\\s+");
        List<String> significantTokens = new ArrayList<>();

        for (String raw : rawTokens) {
            if (raw.isEmpty()) continue;
            if (STOP_WORDS.contains(raw)) continue;
            String stem = stemToken(raw);
            significantTokens.add(stem);
        }

        // 1. Unigram hashing
        for (String token : significantTokens) {
            int hash1 = Math.abs(token.hashCode());
            int idx1 = hash1 % dimension;
            vector[idx1] += 1.5f;

            int hash2 = Math.abs((token + "_alt").hashCode());
            int idx2 = hash2 % dimension;
            vector[idx2] += 0.75f;
        }

        // 2. Bigram hashing
        for (int i = 0; i < significantTokens.size() - 1; i++) {
            String bigram = significantTokens.get(i) + "_" + significantTokens.get(i + 1);
            int hashBi = Math.abs(bigram.hashCode());
            int idxBi = hashBi % dimension;
            vector[idxBi] += 2.5f;
        }

        // 3. Vector normalization (L2)
        double norm = 0.0;
        for (float val : vector) {
            norm += val * val;
        }

        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < dimension; i++) {
                vector[i] /= (float) norm;
            }
        }

        return Embedding.from(vector);
    }
}
