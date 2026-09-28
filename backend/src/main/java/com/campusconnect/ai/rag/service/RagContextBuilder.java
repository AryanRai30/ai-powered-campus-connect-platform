package com.campusconnect.ai.rag.service;

import com.campusconnect.ai.rag.dto.VectorSearchResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Component for building grounded RAG prompts combining retrieved campus knowledge chunks with student queries.
 */
@Component
public class RagContextBuilder {

    public static final String NO_INFO_FOUND_MESSAGE = "I couldn't find this information in the available campus data.";

    /**
     * Constructs a grounded AI prompt incorporating retrieved knowledge context segments.
     * Returns null if chunks is null or empty.
     */
    public String buildGroundedPrompt(String userQuery, List<VectorSearchResult> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("You are the official Campus Connect AI assistant. Answer the student query strictly using ONLY the provided campus knowledge context below.\n\n");
        sb.append("=== OFFICIAL CAMPUS KNOWLEDGE CONTEXT ===\n");

        for (int i = 0; i < chunks.size(); i++) {
            VectorSearchResult chunk = chunks.get(i);
            sb.append("\n--- Knowledge Segment [").append(i + 1).append("] ---\n");

            Map<String, String> meta = chunk.getMetadata();
            if (meta != null) {
                if (meta.containsKey("sourceType")) sb.append("Type: ").append(meta.get("sourceType")).append(" | ");
                if (meta.containsKey("title")) sb.append("Title: ").append(meta.get("title")).append(" | ");
                if (meta.containsKey("targetDepartment")) sb.append("Department: ").append(meta.get("targetDepartment")).append(" | ");
                if (meta.containsKey("targetCourse")) sb.append("Course: ").append(meta.get("targetCourse")).append(" | ");
                if (meta.containsKey("targetYear")) sb.append("Year: ").append(meta.get("targetYear"));
                sb.append("\n");
            }

            sb.append("Content:\n").append(chunk.getText()).append("\n");
        }

        sb.append("\n========================================\n\n");
        sb.append("INSTRUCTIONS FOR AI ASSISTANT:\n");
        sb.append("1. Answer the student's question using ONLY facts stated in the context above.\n");
        sb.append("2. Do NOT use outside knowledge, speculate, or invent any campus dates, rules, or details not present in the context.\n");
        sb.append("3. If the context does not contain enough information to answer the question, respond EXACTLY with:\n");
        sb.append("   \"").append(NO_INFO_FOUND_MESSAGE).append("\"\n\n");
        sb.append("Student Query: ").append(userQuery.trim());

        return sb.toString();
    }
}
