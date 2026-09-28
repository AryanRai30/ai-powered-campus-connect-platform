package com.campusconnect.ai.rag.service;

import com.campusconnect.ai.rag.dto.TextChunkDto;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for splitting raw campus documents/texts into chunked segments for RAG embeddings.
 */
@Service
public class DocumentChunkingService {

    private static final Logger log = LoggerFactory.getLogger(DocumentChunkingService.class);

    private static final int DEFAULT_MAX_CHUNK_SIZE = 500;
    private static final int DEFAULT_MAX_OVERLAP = 50;

    /**
     * Splits input content into TextSegments using specified max size and overlap.
     */
    public List<TextSegment> splitText(String content, Map<String, String> metadataMap, int maxChunkSize, int maxOverlap) {
        if (content == null || content.trim().isEmpty()) {
            return List.of();
        }

        Metadata metadata = metadataMap != null ? Metadata.from(metadataMap) : new Metadata();
        Document document = Document.from(content, metadata);

        var splitter = DocumentSplitters.recursive(
                maxChunkSize > 0 ? maxChunkSize : DEFAULT_MAX_CHUNK_SIZE,
                maxOverlap >= 0 ? maxOverlap : DEFAULT_MAX_OVERLAP
        );

        List<TextSegment> segments = splitter.split(document);
        log.debug("Split document into {} segments (maxChunkSize={}, maxOverlap={})",
                segments.size(), maxChunkSize, maxOverlap);

        return segments;
    }

    /**
     * Convenience method splitting text into TextChunkDtos.
     */
    public List<TextChunkDto> chunkDocument(String documentId, String content, Map<String, String> metadataMap) {
        List<TextSegment> segments = splitText(content, metadataMap, DEFAULT_MAX_CHUNK_SIZE, DEFAULT_MAX_OVERLAP);
        List<TextChunkDto> chunks = new ArrayList<>();

        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            chunks.add(TextChunkDto.builder()
                    .chunkId(documentId + "-chunk-" + i)
                    .documentId(documentId)
                    .text(segment.text())
                    .chunkIndex(i)
                    .metadata(metadataMap)
                    .build());
        }

        return chunks;
    }
}
