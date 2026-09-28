package com.campusconnect.ai.rag;

import com.campusconnect.ai.rag.dto.VectorSearchResult;
import com.campusconnect.ai.rag.service.RagContextBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RagContextBuilderTest {

    private RagContextBuilder contextBuilder;

    @BeforeEach
    void setUp() {
        contextBuilder = new RagContextBuilder();
    }

    @Test
    @DisplayName("Should build grounded prompt with context segments and instructions")
    void testBuildGroundedPrompt() {
        VectorSearchResult chunk1 = VectorSearchResult.builder()
                .segmentId("chunk-1")
                .text("Library quiet hours are 9 PM to 6 AM.")
                .score(0.88)
                .metadata(Map.of("sourceType", "ANNOUNCEMENT", "title", "Library Notice", "targetDepartment", "ALL"))
                .build();

        VectorSearchResult chunk2 = VectorSearchResult.builder()
                .segmentId("chunk-2")
                .text("Main library offers 24/7 study rooms during exam week.")
                .score(0.82)
                .metadata(Map.of("sourceType", "ACADEMIC_RESOURCE", "title", "Library Facilities"))
                .build();

        String prompt = contextBuilder.buildGroundedPrompt("What are the library hours?", List.of(chunk1, chunk2));

        assertNotNull(prompt);
        assertTrue(prompt.contains("Library Notice"));
        assertTrue(prompt.contains("Library quiet hours are 9 PM to 6 AM."));
        assertTrue(prompt.contains(RagContextBuilder.NO_INFO_FOUND_MESSAGE));
        assertTrue(prompt.contains("Student Query: What are the library hours?"));
    }

    @Test
    @DisplayName("Should return null prompt when chunks list is null or empty")
    void testBuildGroundedPromptEmpty() {
        assertNull(contextBuilder.buildGroundedPrompt("Any question?", null));
        assertNull(contextBuilder.buildGroundedPrompt("Any question?", List.of()));
    }
}
