package com.campusconnect.ai.rag;

import com.campusconnect.ai.rag.service.EmbeddingService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingServiceTest {

    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService();
    }

    @Test
    @DisplayName("Should generate valid fallback embedding vector for text segment")
    void testEmbedFallback() {
        TextSegment segment = TextSegment.from("Campus library operating hours: 8 AM to 10 PM daily.");
        Embedding embedding = embeddingService.embed(segment);

        assertNotNull(embedding);
        assertNotNull(embedding.vector());
        assertEquals(768, embedding.dimension());
    }

    @Test
    @DisplayName("Should generate normalized embeddings for multiple text segments")
    void testEmbedAllFallback() {
        List<TextSegment> segments = List.of(
                TextSegment.from("Student registration guidelines."),
                TextSegment.from("Faculty office location index.")
        );

        List<Embedding> embeddings = embeddingService.embedAll(segments);
        assertNotNull(embeddings);
        assertEquals(2, embeddings.size());
        assertEquals(768, embeddings.get(0).dimension());
        assertEquals(768, embeddings.get(1).dimension());
    }
}
