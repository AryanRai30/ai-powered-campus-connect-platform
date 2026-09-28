package com.campusconnect.ai.rag.controller;

import com.campusconnect.ai.rag.dto.RagQueryRequest;
import com.campusconnect.ai.rag.dto.RagQueryResponse;
import com.campusconnect.ai.rag.service.RagRetrievalService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing REST endpoints for Grounded RAG Knowledge Base Queries.
 */
@RestController
@RequestMapping("/api/ai/rag")
public class RagController {

    private static final Logger log = LoggerFactory.getLogger(RagController.class);

    private final RagRetrievalService ragRetrievalService;

    @Autowired
    public RagController(RagRetrievalService ragRetrievalService) {
        this.ragRetrievalService = ragRetrievalService;
    }

    /**
     * POST /api/ai/rag/query
     * Queries the Qdrant campus knowledge base and returns grounded AI answers.
     */
    @PostMapping("/query")
    public ResponseEntity<RagQueryResponse> queryCampusKnowledge(@Valid @RequestBody RagQueryRequest request) {
        String email = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            email = authentication.getName();
        }

        log.info("Received RAG knowledge query from user '{}': {}", email != null ? email : "anonymous", request.getQuery());
        RagQueryResponse response = ragRetrievalService.queryCampusKnowledge(request, email);
        return ResponseEntity.ok(response);
    }
}
