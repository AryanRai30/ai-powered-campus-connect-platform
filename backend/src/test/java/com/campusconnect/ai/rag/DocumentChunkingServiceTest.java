package com.campusconnect.ai.rag;

import com.campusconnect.ai.rag.dto.TextChunkDto;
import com.campusconnect.ai.rag.service.DocumentChunkingService;
import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DocumentChunkingServiceTest {

    private DocumentChunkingService chunkingService;

    @BeforeEach
    void setUp() {
        chunkingService = new DocumentChunkingService();
    }

    @Test
    @DisplayName("Should split document text into segments")
    void testSplitText() {
        String content = "Campus Connect is an AI-powered platform for students, faculty, and academic resources. "
                + "It provides event tracking, club activities, and campus knowledge base retrieval.";
        Map<String, String> metadata = Map.of("category", "handbook", "author", "admin");

        List<TextSegment> segments = chunkingService.splitText(content, metadata, 50, 10);
        assertNotNull(segments);
        assertFalse(segments.isEmpty());
        assertTrue(segments.size() > 1);
        assertEquals("handbook", segments.get(0).metadata().get("category"));
    }

    @Test
    @DisplayName("Should return empty list for null or empty content")
    void testSplitEmptyContent() {
        List<TextSegment> segmentsNull = chunkingService.splitText(null, null, 100, 10);
        assertTrue(segmentsNull.isEmpty());

        List<TextSegment> segmentsEmpty = chunkingService.splitText("   ", null, 100, 10);
        assertTrue(segmentsEmpty.isEmpty());
    }

    @Test
    @DisplayName("Should chunk document into TextChunkDtos with metadata and IDs")
    void testChunkDocument() {
        String content = "Computer Science Department course syllabus overview for 2026 academic year.";
        List<TextChunkDto> chunks = chunkingService.chunkDocument("doc-101", content, Map.of("department", "CS"));

        assertNotNull(chunks);
        assertEquals(1, chunks.size());
        assertEquals("doc-101-chunk-0", chunks.get(0).getChunkId());
        assertEquals("doc-101", chunks.get(0).getDocumentId());
        assertEquals(0, chunks.get(0).getChunkIndex());
        assertEquals("CS", chunks.get(0).getMetadata().get("department"));
    }
}
