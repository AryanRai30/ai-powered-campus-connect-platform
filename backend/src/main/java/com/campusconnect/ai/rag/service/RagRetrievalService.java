package com.campusconnect.ai.rag.service;

import com.campusconnect.ai.rag.dto.DocumentIngestionRequest;
import com.campusconnect.ai.rag.dto.RagQueryRequest;
import com.campusconnect.ai.rag.dto.RagQueryResponse;
import com.campusconnect.ai.rag.dto.VectorSearchResult;
import com.campusconnect.ai.rag.mapper.CampusDocumentConverter;
import com.campusconnect.ai.service.AiAssistantService;
import com.campusconnect.entity.*;
import com.campusconnect.repository.*;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service orchestrating RAG query retrieval, authorization filtering, grounded context construction,
 * and AI response generation. Incorporates a two-level retrieval architecture: Qdrant vector retrieval first,
 * with direct MySQL repository fallback when vector store returns zero usable chunks.
 */
@Service
public class RagRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(RagRetrievalService.class);

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final AiAssistantService aiAssistantService;
    private final RagContextBuilder ragContextBuilder;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;

    private final AnnouncementRepository announcementRepository;
    private final EventRepository eventRepository;
    private final AcademicResourceRepository academicResourceRepository;
    private final ClubRepository clubRepository;
    private final OpportunityRepository opportunityRepository;
    private final CampusDocumentConverter documentConverter;

    @Autowired
    public RagRetrievalService(EmbeddingService embeddingService,
                               VectorStoreService vectorStoreService,
                               AiAssistantService aiAssistantService,
                               RagContextBuilder ragContextBuilder,
                               UserRepository userRepository,
                               StudentProfileRepository studentProfileRepository,
                               AnnouncementRepository announcementRepository,
                               EventRepository eventRepository,
                               AcademicResourceRepository academicResourceRepository,
                               ClubRepository clubRepository,
                               OpportunityRepository opportunityRepository,
                               CampusDocumentConverter documentConverter) {
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.aiAssistantService = aiAssistantService;
        this.ragContextBuilder = ragContextBuilder;
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.announcementRepository = announcementRepository;
        this.eventRepository = eventRepository;
        this.academicResourceRepository = academicResourceRepository;
        this.clubRepository = clubRepository;
        this.opportunityRepository = opportunityRepository;
        this.documentConverter = documentConverter;
    }

    /**
     * Detects if a query is a campus-domain question vs a general knowledge/technical question.
     */
    public boolean isCampusQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        String q = query.trim().toLowerCase();

        // 1. Explicit campus keywords
        String[] campusKeywords = {
                "campus", "college", "university", "club", "clubs", "event", "events",
                "announcement", "announcements", "academic", "academics", "resource", "resources",
                "internship", "internships", "opportunity", "opportunities", "placement", "placements",
                "salesforce", "faculty", "professor", "syllabus", "coursework", "semester",
                "gpa", "credits", "hostel", "canteen", "library", "exam", "exams", "midterm",
                "register", "registration", "organizer", "department", "dept", "schedule",
                "guideline", "guidelines", "lab", "labs", "fee", "fees", "admission", "admissions",
                "notice", "notices", "rule", "rules", "policy", "policies"
        };

        for (String kw : campusKeywords) {
            if (q.contains(kw)) {
                return true;
            }
        }

        if (q.contains("available") || q.contains("upcoming") || q.contains("happening") || q.contains("ongoing") || q.contains("apply")) {
            return true;
        }

        // 2. General tech / non-campus query patterns
        if (q.matches("^(what is|explain|how does|define|difference between|what are) (java|spring|spring boot|jwt|react|rag|microservices|python|javascript|typescript|c\\+\\+|sql|html|css|docker|kubernetes|git|rest|api|json|oauth|database|oop|recursion|pointers|operating system|linux|windows|architecture|design patterns?|data structures?).*")) {
            return false;
        }

        return false;
    }

    /**
     * Executes grounded RAG retrieval and AI answer generation for a student query using two-level retrieval.
     */
    public RagQueryResponse queryCampusKnowledge(RagQueryRequest request, String currentUserEmail) {
        if (request == null || request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            return RagQueryResponse.builder()
                    .query(request != null ? request.getQuery() : null)
                    .answer("Query message cannot be empty.")
                    .grounded(false)
                    .retrievedChunksCount(0)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        String userQuery = request.getQuery().trim();

        // Automatic Routing Check: If query is a general non-campus query sent to RAG endpoint, route to normal LLM AI service
        if (!isCampusQuery(userQuery)) {
            log.info("RAG query endpoint received general non-campus query '{}'. Directing to normal AI answer.", userQuery);
            String aiAnswer = aiAssistantService.generateResponse(userQuery);
            return RagQueryResponse.builder()
                    .query(userQuery)
                    .answer(aiAnswer)
                    .grounded(false)
                    .retrievedChunksCount(0)
                    .matchedChunks(List.of())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        int maxResults = request.getMaxResults() != null && request.getMaxResults() > 0 ? request.getMaxResults() : 5;
        double minScore = request.getMinScoreThreshold() != null ? request.getMinScoreThreshold() : 0.35;

        // Resolve student context metadata for targeting/authorization
        String dept = request.getDepartment();
        String course = request.getCourse();
        Integer year = request.getYear();
        Integer semester = request.getSemester();

        if ((dept == null || dept.isEmpty()) && currentUserEmail != null && !currentUserEmail.trim().isEmpty()) {
            User user = userRepository.findByEmail(currentUserEmail.trim()).orElse(null);
            if (user != null) {
                StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
                if (profile != null) {
                    if (dept == null) dept = profile.getDepartment();
                    if (course == null) course = profile.getCourse();
                    if (year == null && profile.getYear() != null) {
                        try { year = Integer.parseInt(profile.getYear().trim()); } catch (Exception ignored) {}
                    }
                    if (semester == null && profile.getSemester() != null) {
                        try { semester = Integer.parseInt(profile.getSemester().trim()); } catch (Exception ignored) {}
                    }
                }
            }
        }

        // LEVEL 1: Qdrant Vector Retrieval
        List<VectorSearchResult> authorizedChunks = new ArrayList<>();
        try {
            TextSegment querySegment = TextSegment.from(userQuery);
            Embedding queryEmbedding = embeddingService.embed(querySegment);
            List<EmbeddingMatch<TextSegment>> rawMatches = vectorStoreService.findRelevant(queryEmbedding, maxResults, minScore);

            List<VectorSearchResult> candidateChunks = new ArrayList<>();
            for (EmbeddingMatch<TextSegment> match : rawMatches) {
                TextSegment embedded = match.embedded();
                Map<String, String> metaMap = new HashMap<>();
                if (embedded != null && embedded.metadata() != null) {
                    embedded.metadata().toMap().forEach((k, v) -> metaMap.put(k, v != null ? String.valueOf(v) : null));
                }

                candidateChunks.add(VectorSearchResult.builder()
                        .segmentId(match.embeddingId())
                        .text(embedded != null ? embedded.text() : "")
                        .score(match.score())
                        .metadata(metaMap)
                        .build());
            }

            authorizedChunks = filterAuthorizedChunks(candidateChunks, dept, course, year, semester);
        } catch (Exception e) {
            log.warn("Level 1 Qdrant vector retrieval failed or returned 0 results: {}", e.getMessage());
        }

        // LEVEL 2: MySQL Repository Fallback Retrieval (if Level 1 yielded 0 authorized chunks)
        if (authorizedChunks.isEmpty()) {
            log.info("Level 1 Qdrant RAG query '{}' yielded 0 authorized chunks. Performing Level 2 MySQL Fallback Retrieval...", userQuery);
            authorizedChunks = performFallbackRetrieval(userQuery, dept, course, year, semester);
        }

        // Handle case when both Level 1 and Level 2 return no authorized chunks
        if (authorizedChunks.isEmpty()) {
            log.info("RAG query '{}' yielded 0 authorized campus knowledge chunks after Qdrant and MySQL fallback.", userQuery);
            return RagQueryResponse.builder()
                    .query(userQuery)
                    .answer(RagContextBuilder.NO_INFO_FOUND_MESSAGE)
                    .grounded(false)
                    .retrievedChunksCount(0)
                    .matchedChunks(List.of())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        // Build grounded context prompt & call LLM AI service
        String groundedPrompt = ragContextBuilder.buildGroundedPrompt(userQuery, authorizedChunks);
        String aiAnswer = aiAssistantService.generateResponse(groundedPrompt);

        log.info("RAG query '{}' successfully answered using {} grounded chunks.", userQuery, authorizedChunks.size());

        return RagQueryResponse.builder()
                .query(userQuery)
                .answer(aiAnswer)
                .grounded(true)
                .retrievedChunksCount(authorizedChunks.size())
                .matchedChunks(authorizedChunks)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Fallback retrieval directly from existing MySQL repositories when Qdrant vector search yields 0 authorized chunks.
     * Uses ONLY published=true AND active=true campus data, applies student authorization rules,
     * and matches meaningful campus keywords.
     */
    public List<VectorSearchResult> performFallbackRetrieval(String userQuery, String dept, String course, Integer year, Integer semester) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return List.of();
        }

        String qLower = userQuery.trim().toLowerCase();

        boolean isClubQuery = qLower.contains("club") || qLower.contains("clubs") || qLower.contains("society") || qLower.contains("societies") || qLower.contains("organization");
        boolean isResourceQuery = qLower.contains("resource") || qLower.contains("resources") || qLower.contains("academic") || qLower.contains("academics") || qLower.contains("study") || qLower.contains("notes") || qLower.contains("syllabus") || qLower.contains("book") || qLower.contains("books") || qLower.contains("paper") || qLower.contains("papers") || qLower.contains("subject");
        boolean isOppQuery = qLower.contains("opportunity") || qLower.contains("opportunities") || qLower.contains("internship") || qLower.contains("internships") || qLower.contains("placement") || qLower.contains("placements") || qLower.contains("job") || qLower.contains("jobs") || qLower.contains("hiring") || qLower.contains("career");
        boolean isEventQuery = qLower.contains("event") || qLower.contains("events") || qLower.contains("workshop") || qLower.contains("workshops") || qLower.contains("fest") || qLower.contains("seminar") || qLower.contains("hackathon") || qLower.contains("webinar") || qLower.contains("conference") || qLower.contains("competition");
        boolean isAnnouncementQuery = qLower.contains("announcement") || qLower.contains("announcements") || qLower.contains("notice") || qLower.contains("notices") || qLower.contains("news") || qLower.contains("update") || qLower.contains("updates") || qLower.contains("circular");

        boolean fetchAll = !isClubQuery && !isResourceQuery && !isOppQuery && !isEventQuery && !isAnnouncementQuery;

        List<DocumentIngestionRequest> fallbackRequests = new ArrayList<>();

        if (isClubQuery || fetchAll) {
            try {
                List<Club> clubs = clubRepository.findByPublishedTrueAndActiveTrue();
                for (Club c : clubs) {
                    DocumentIngestionRequest req = documentConverter.convertClub(c);
                    if (req != null) fallbackRequests.add(req);
                }
            } catch (Exception e) {
                log.error("Fallback retrieval failed for clubs: {}", e.getMessage());
            }
        }

        if (isResourceQuery || fetchAll) {
            try {
                List<AcademicResource> resources = academicResourceRepository.findByPublishedTrueAndActiveTrue();
                for (AcademicResource r : resources) {
                    DocumentIngestionRequest req = documentConverter.convertAcademicResource(r);
                    if (req != null) fallbackRequests.add(req);
                }
            } catch (Exception e) {
                log.error("Fallback retrieval failed for academic resources: {}", e.getMessage());
            }
        }

        if (isOppQuery || fetchAll) {
            try {
                List<Opportunity> opportunities = opportunityRepository.findByPublishedTrueAndActiveTrue();
                for (Opportunity o : opportunities) {
                    DocumentIngestionRequest req = documentConverter.convertOpportunity(o);
                    if (req != null) fallbackRequests.add(req);
                }
            } catch (Exception e) {
                log.error("Fallback retrieval failed for opportunities: {}", e.getMessage());
            }
        }

        if (isEventQuery || fetchAll) {
            try {
                List<Event> events = eventRepository.findByPublishedTrueAndActiveTrue();
                for (Event ev : events) {
                    DocumentIngestionRequest req = documentConverter.convertEvent(ev);
                    if (req != null) fallbackRequests.add(req);
                }
            } catch (Exception e) {
                log.error("Fallback retrieval failed for events: {}", e.getMessage());
            }
        }

        if (isAnnouncementQuery || fetchAll) {
            try {
                List<Announcement> announcements = announcementRepository.findByPublishedTrueAndActiveTrue();
                for (Announcement a : announcements) {
                    DocumentIngestionRequest req = documentConverter.convertAnnouncement(a);
                    if (req != null) fallbackRequests.add(req);
                }
            } catch (Exception e) {
                log.error("Fallback retrieval failed for announcements: {}", e.getMessage());
            }
        }

        // Convert DocumentIngestionRequests to VectorSearchResult items
        List<VectorSearchResult> candidates = new ArrayList<>();
        for (DocumentIngestionRequest req : fallbackRequests) {
            candidates.add(VectorSearchResult.builder()
                    .segmentId(req.getDocumentId())
                    .text(req.getContent())
                    .score(1.0)
                    .metadata(req.getMetadata() != null ? req.getMetadata() : new HashMap<>())
                    .build());
        }

        // Apply student authorization rules (department, course, year, semester, ALL)
        return filterAuthorizedChunks(candidates, dept, course, year, semester);
    }

    /**
     * Filters candidate chunks ensuring student only receives campus content targeted to their profile or public.
     */
    public List<VectorSearchResult> filterAuthorizedChunks(List<VectorSearchResult> candidateChunks,
                                                          String dept, String course,
                                                          Integer year, Integer sem) {
        if (candidateChunks == null || candidateChunks.isEmpty()) {
            return List.of();
        }

        List<VectorSearchResult> authorized = new ArrayList<>();
        for (VectorSearchResult chunk : candidateChunks) {
            if (isChunkAuthorizedForStudent(chunk, dept, course, year, sem)) {
                authorized.add(chunk);
            }
        }
        return authorized;
    }

    /**
     * Evaluates authorization rules for a single chunk against student profile attributes.
     */
    public boolean isChunkAuthorizedForStudent(VectorSearchResult chunk, String dept, String course, Integer year, Integer sem) {
        if (chunk == null || chunk.getMetadata() == null) {
            return true;
        }

        Map<String, String> meta = chunk.getMetadata();

        String targetDept = meta.get("targetDepartment");
        if (targetDept == null || targetDept.trim().isEmpty()) {
            targetDept = meta.get("department");
        }

        if (!isAllTarget(targetDept)) {
            if (dept != null && !dept.trim().isEmpty() && !dept.trim().equalsIgnoreCase(targetDept.trim())) {
                return false;
            }
        }

        String targetCourse = meta.get("targetCourse");
        if (!isAllTarget(targetCourse)) {
            if (course != null && !course.trim().isEmpty() && !course.trim().equalsIgnoreCase(targetCourse.trim())) {
                return false;
            }
        }

        String targetYearStr = meta.get("targetYear");
        if (targetYearStr != null && !targetYearStr.trim().isEmpty() && !isAllTarget(targetYearStr)) {
            try {
                int targetYear = Integer.parseInt(targetYearStr.trim());
                if (targetYear > 0 && year != null && year > 0 && year != targetYear) {
                    return false;
                }
            } catch (NumberFormatException ignored) {}
        }

        String targetSemStr = meta.get("targetSemester");
        if (targetSemStr != null && !targetSemStr.trim().isEmpty() && !isAllTarget(targetSemStr)) {
            try {
                int targetSem = Integer.parseInt(targetSemStr.trim());
                if (targetSem > 0 && sem != null && sem > 0 && sem != targetSem) {
                    return false;
                }
            } catch (NumberFormatException ignored) {}
        }

        return true;
    }

    private boolean isAllTarget(String target) {
        if (target == null || target.trim().isEmpty()) {
            return true;
        }
        String t = target.trim().toUpperCase();
        return t.equals("ALL") ||
               t.equals("ALL DEPARTMENTS") ||
               t.equals("ALL COURSES") ||
               t.equals("ALL YEARS") ||
               t.equals("ALL SEMESTERS") ||
               t.equals("ALL_DEPARTMENTS") ||
               t.equals("ALL_COURSES") ||
               t.startsWith("ALL ") ||
               t.equals("EVERYONE") ||
               t.equals("ANY") ||
               t.equals("GENERAL");
    }
}

