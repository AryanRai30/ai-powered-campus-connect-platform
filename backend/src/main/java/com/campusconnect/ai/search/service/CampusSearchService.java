package com.campusconnect.ai.search.service;

import com.campusconnect.ai.rag.dto.VectorSearchResult;
import com.campusconnect.ai.rag.service.EmbeddingService;
import com.campusconnect.ai.rag.service.RagRetrievalService;
import com.campusconnect.ai.rag.service.VectorStoreService;
import com.campusconnect.ai.search.dto.CampusSearchRequest;
import com.campusconnect.ai.search.dto.CampusSearchResultItem;
import com.campusconnect.ai.search.dto.CampusSearchResponse;
import com.campusconnect.entity.StudentProfile;
import com.campusconnect.entity.User;
import com.campusconnect.repository.StudentProfileRepository;
import com.campusconnect.repository.UserRepository;
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
import java.util.stream.Collectors;

/**
 * Dedicated service for Phase 11.4 Semantic Campus Search.
 * Reuses existing EmbeddingService, VectorStoreService (Qdrant campus_knowledge_base collection),
 * RagRetrievalService authorization filtering, and MySQL fallback.
 */
@Service
public class CampusSearchService {

    private static final Logger log = LoggerFactory.getLogger(CampusSearchService.class);

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final RagRetrievalService ragRetrievalService;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;

    @Autowired
    public CampusSearchService(EmbeddingService embeddingService,
                               VectorStoreService vectorStoreService,
                               RagRetrievalService ragRetrievalService,
                               UserRepository userRepository,
                               StudentProfileRepository studentProfileRepository) {
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.ragRetrievalService = ragRetrievalService;
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    /**
     * Executes semantic vector search against campus knowledge base with authorization filtering.
     */
    public CampusSearchResponse searchCampusKnowledge(CampusSearchRequest request, String currentUserEmail) {
        if (request == null || request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            return CampusSearchResponse.builder()
                    .query(request != null ? request.getQuery() : null)
                    .totalResults(0)
                    .results(List.of())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        String userQuery = request.getQuery().trim();
        int maxResults = request.getMaxResults() != null && request.getMaxResults() > 0 ? request.getMaxResults() : 15;
        double minScore = request.getMinScoreThreshold() != null && request.getMinScoreThreshold() > 0.0
                ? request.getMinScoreThreshold() : 0.55;

        // Domain Intent Check: reject non-campus / general knowledge queries
        if (!isCampusDomainQuery(userQuery, request.getCategory())) {
            log.info("Semantic campus search for query '{}' rejected as non-campus/unrelated domain.", userQuery);
            return CampusSearchResponse.builder()
                    .query(userQuery)
                    .totalResults(0)
                    .results(List.of())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        // Resolve student context for targeting/authorization rules
        String dept = null;
        String course = null;
        Integer year = null;
        Integer semester = null;

        if (currentUserEmail != null && !currentUserEmail.trim().isEmpty()) {
            User user = userRepository.findByEmail(currentUserEmail.trim()).orElse(null);
            if (user != null) {
                StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
                if (profile != null) {
                    dept = profile.getDepartment();
                    course = profile.getCourse();
                    if (profile.getYear() != null) {
                        try { year = Integer.parseInt(profile.getYear().trim()); } catch (Exception ignored) {}
                    }
                    if (profile.getSemester() != null) {
                        try { semester = Integer.parseInt(profile.getSemester().trim()); } catch (Exception ignored) {}
                    }
                }
            }
        }

        List<String> specificTerms = extractSpecificQueryTerms(userQuery);
        List<VectorSearchResult> authorizedChunks = new ArrayList<>();

        // Level 1: Qdrant Vector Search with dynamic concept evaluation and calibrated similarity threshold
        try {
            TextSegment querySegment = TextSegment.from(userQuery);
            Embedding queryEmbedding = embeddingService.embed(querySegment);
            List<EmbeddingMatch<TextSegment>> rawMatches = vectorStoreService.findRelevant(queryEmbedding, maxResults * 3, 0.40);

            List<VectorSearchResult> candidateChunks = new ArrayList<>();
            for (EmbeddingMatch<TextSegment> match : rawMatches) {
                TextSegment embedded = match.embedded();
                Map<String, String> metaMap = new HashMap<>();
                if (embedded != null && embedded.metadata() != null) {
                    embedded.metadata().toMap().forEach((k, v) -> metaMap.put(k, v != null ? String.valueOf(v) : null));
                }

                VectorSearchResult rawChunk = VectorSearchResult.builder()
                        .segmentId(match.embeddingId())
                        .text(embedded != null ? embedded.text() : "")
                        .score(match.score())
                        .metadata(metaMap)
                        .build();

                double relScore = calculateRelevanceScore(userQuery, specificTerms, rawChunk);
                if (relScore >= minScore) {
                    candidateChunks.add(VectorSearchResult.builder()
                            .segmentId(rawChunk.getSegmentId())
                            .text(rawChunk.getText())
                            .score(relScore)
                            .metadata(rawChunk.getMetadata())
                            .build());
                }
            }

            authorizedChunks = ragRetrievalService.filterAuthorizedChunks(candidateChunks, dept, course, year, semester);
        } catch (Exception e) {
            log.warn("Level 1 Qdrant vector search failed during campus search: {}", e.getMessage());
        }

        // Level 2: MySQL Fallback Retrieval if Level 1 returned 0 authorized chunks (applying SAME relevance rules)
        if (authorizedChunks.isEmpty()) {
            log.info("Qdrant semantic search for '{}' yielded 0 authorized hits. Performing MySQL fallback retrieval...", userQuery);
            List<VectorSearchResult> fallbackCandidates = ragRetrievalService.performFallbackRetrieval(userQuery, dept, course, year, semester);

            List<VectorSearchResult> filteredFallback = new ArrayList<>();
            for (VectorSearchResult fallbackChunk : fallbackCandidates) {
                double relScore = calculateRelevanceScore(userQuery, specificTerms, fallbackChunk);
                if (relScore >= minScore) {
                    filteredFallback.add(VectorSearchResult.builder()
                            .segmentId(fallbackChunk.getSegmentId())
                            .text(fallbackChunk.getText())
                            .score(relScore)
                            .metadata(fallbackChunk.getMetadata())
                            .build());
                }
            }
            authorizedChunks = filteredFallback;
        }

        // Map authorized chunks into ranked CampusSearchResultItem instances
        List<CampusSearchResultItem> resultItems = new ArrayList<>();
        for (VectorSearchResult chunk : authorizedChunks) {
            Map<String, String> meta = chunk.getMetadata() != null ? chunk.getMetadata() : new HashMap<>();
            String sourceType = meta.getOrDefault("sourceType", meta.getOrDefault("category", "CAMPUS_RECORD"));
            String title = meta.get("title");

            if (title == null || title.trim().isEmpty()) {
                title = extractTitleFromContent(chunk.getText(), sourceType);
            }

            CampusSearchResultItem item = CampusSearchResultItem.builder()
                    .id(chunk.getSegmentId())
                    .title(title)
                    .content(chunk.getText())
                    .sourceType(sourceType)
                    .score(Math.round(chunk.getScore() * 100.0) / 100.0)
                    .category(meta.get("category"))
                    .sourceId(meta.get("sourceId"))
                    .sourceUrl(meta.get("sourceUrl"))
                    .metadata(meta)
                    .build();

            resultItems.add(item);
        }

        // Filter by category / sourceType if specified in request
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty() && !"ALL".equalsIgnoreCase(request.getCategory().trim())) {
            String filterCat = request.getCategory().trim().toUpperCase();
            resultItems = resultItems.stream()
                    .filter(item -> filterCat.equalsIgnoreCase(item.getSourceType()) || filterCat.equalsIgnoreCase(item.getCategory()))
                    .collect(Collectors.toList());
        }

        // Sort descending by score
        resultItems.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // Limit results
        if (resultItems.size() > maxResults) {
            resultItems = resultItems.subList(0, maxResults);
        }

        log.info("Semantic campus search for query '{}' (user={}) returned {} ranked results.",
                userQuery, currentUserEmail != null ? currentUserEmail : "anonymous", resultItems.size());

        return CampusSearchResponse.builder()
                .query(userQuery)
                .totalResults(resultItems.size())
                .results(resultItems)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Determines whether a natural-language search query is relevant to campus domain knowledge.
     */
    public boolean isCampusDomainQuery(String query, String categoryFilter) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }

        if (categoryFilter != null && !categoryFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(categoryFilter.trim())) {
            return true;
        }

        String q = query.trim().toLowerCase();

        boolean isUnrelatedPattern = q.matches(".*\\b(" +
                "restaurant|restaurants|cafe|cafes|dining|food in|" +
                "capital of|population of|currency of|" +
                "spacex|nasa|mars mission|moon landing|" +
                "symptoms of|treatment for|diabetes|disease|medicine|" +
                "cryptocurrency|crypto mining|bitcoin|blockchain mining|" +
                "iphone|samsung galaxy|macbook|" +
                "roman empire|ancient history|world war" +
                ")\\b.*");

        if (isUnrelatedPattern) {
            boolean hasExplicitCampusContext = q.contains("campus") || q.contains("college") || q.contains("university")
                    || q.contains("club") || q.contains("event") || q.contains("notice") || q.contains("announcement")
                    || q.contains("internship") || q.contains("syllabus") || q.contains("department");
            if (!hasExplicitCampusContext) {
                return false;
            }
        }

        return containsCampusDomainTerm(q);
    }

    private boolean containsCampusDomainTerm(String q) {
        String[] campusTerms = {
            "campus", "college", "university", "student", "students", "faculty", "professor", "professors",
            "department", "dept", "hostel", "canteen", "library", "lab", "labs", "admission", "admissions",
            "fee", "fees", "tuition", "scholarship", "scholarships", "semester", "sem", "syllabus", "curriculum",
            "course", "courses", "subject", "subjects", "exam", "exams", "examination", "midterm", "midterms",
            "gpa", "cgpa", "credit", "credits", "grade", "grades", "assignment", "assignments", "project", "projects",
            "study", "notes", "lecture", "lectures", "tutorial", "textbook", "materials", "resources",
            "club", "clubs", "society", "societies", "community", "communities", "enthusiast", "enthusiasts",
            "chapter", "chapters", "group", "groups", "team", "teams", "organization", "organisations",
            "event", "events", "activity", "activities", "workshop", "workshops", "fest", "fests", "festival",
            "seminar", "seminars", "webinar", "webinars", "hackathon", "hackathons", "competition", "competitions",
            "contest", "meetup", "conference", "exhibition", "session", "program", "schedule",
            "announcement", "announcements", "notice", "notices", "circular", "circulars", "bulletin", "news",
            "update", "updates", "alert",
            "opportunity", "opportunities", "intern", "interns", "internship", "internships", "job", "jobs",
            "placement", "placements", "career", "careers", "hiring", "vacancy", "vacancies", "opening", "openings",
            "role", "roles", "stipend", "recruitment",
            "programming", "coding", "developer", "development", "software", "backend", "frontend", "fullstack",
            "web dev", "app dev", "machine learning", "data science", "cybersecurity", "robotics"
        };

        for (String term : campusTerms) {
            if (q.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private static final java.util.Set<String> GENERIC_CATEGORY_TERMS = java.util.Set.of(
            "internship", "internships", "opportunity", "opportunities",
            "event", "events", "club", "clubs", "resource", "resources",
            "student", "students", "available", "campus", "development",
            "technology", "tech", "academics", "academic", "study", "notes",
            "notice", "notices", "announcement", "announcements", "details",
            "society", "societies", "organization", "organisations", "group", "groups",
            "program", "programs", "schedule", "schedules", "activity", "activities",
            "material", "materials", "information", "info", "help", "workshop", "workshops",
            "session", "sessions", "hiring", "opening", "openings", "placement", "placements",
            "role", "roles", "stipend", "recruitment", "for", "in", "of", "to", "the", "a", "an",
            "and", "or", "with", "looking", "need", "find", "search", "list", "show",
            "upcoming", "recent", "latest", "official", "all", "any", "some", "new", "is", "are", "me", "my"
    );

    private static final Map<String, List<String>> CONCEPT_SYNONYMS = Map.ofEntries(
            Map.entry("programming", List.of("coding", "code", "developer", "software", "dev", "programming")),
            Map.entry("coding", List.of("programming", "code", "developer", "software", "dev")),
            Map.entry("developer", List.of("development", "dev", "engineer", "engineering", "programming", "coding")),
            Map.entry("sports", List.of("athletics", "sport", "games", "recreation", "fitness", "cricket", "football", "basketball")),
            Map.entry("athletics", List.of("sports", "sport", "games")),
            Map.entry("ai", List.of("artificial intelligence", "machine learning", "ml", "deep learning", "data science")),
            Map.entry("python", List.of("py", "django", "flask", "fastapi", "python")),
            Map.entry("java", List.of("spring", "springboot", "spring boot", "java")),
            Map.entry("frontend", List.of("front-end", "front end", "ui", "ux", "react", "angular", "vue", "web dev", "web development")),
            Map.entry("backend", List.of("back-end", "back end", "server", "api", "spring", "node", "express", "sql", "database"))
    );

    private List<String> extractSpecificQueryTerms(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        String[] tokens = query.toLowerCase().replaceAll("[^a-z0-9+#\\s]", " ").split("\\s+");
        List<String> specificTerms = new ArrayList<>();
        for (String token : tokens) {
            token = token.trim();
            if (token.length() > 1 && !GENERIC_CATEGORY_TERMS.contains(token)) {
                specificTerms.add(token);
            }
        }
        return specificTerms;
    }

    private boolean isConceptContradicted(String queryLower, List<String> specificTerms, String documentTextLower) {
        if (documentTextLower == null || documentTextLower.isEmpty()) {
            return false;
        }

        // 1. Frontend vs Backend
        boolean queryWantsFrontend = specificTerms.contains("frontend") || queryLower.contains("frontend") || queryLower.contains("front-end");
        boolean queryWantsBackend = specificTerms.contains("backend") || queryLower.contains("backend") || queryLower.contains("back-end");

        boolean docIsBackend = documentTextLower.contains("backend") || documentTextLower.contains("back-end");
        boolean docIsFrontend = documentTextLower.contains("frontend") || documentTextLower.contains("front-end");

        if (queryWantsFrontend && !queryWantsBackend) {
            if (docIsBackend && !docIsFrontend) {
                return true;
            }
        }
        if (queryWantsBackend && !queryWantsFrontend) {
            if (docIsFrontend && !docIsBackend) {
                return true;
            }
        }

        // 2. Java vs Python
        boolean queryWantsJava = specificTerms.contains("java") || queryLower.contains("java");
        boolean queryWantsPython = specificTerms.contains("python") || queryLower.contains("python");

        boolean docIsJava = documentTextLower.contains("java");
        boolean docIsPython = documentTextLower.contains("python");

        if (queryWantsJava && !queryWantsPython) {
            if (docIsPython && !docIsJava) {
                return true;
            }
        }
        if (queryWantsPython && !queryWantsJava) {
            if (docIsJava && !docIsPython) {
                return true;
            }
        }

        // 3. Sports vs Tech/Coding
        boolean queryWantsSports = specificTerms.contains("sports") || queryLower.contains("sports") || queryLower.contains("sport");
        boolean queryWantsTechCoding = specificTerms.contains("coding") || specificTerms.contains("programming") || specificTerms.contains("ai");

        boolean docIsTechCoding = documentTextLower.contains("coding") || documentTextLower.contains("programming") || documentTextLower.contains("developer") || documentTextLower.contains("software") || documentTextLower.contains("tech");
        boolean docIsSports = documentTextLower.contains("sports") || documentTextLower.contains("sport") || documentTextLower.contains("athletics") || documentTextLower.contains("cricket") || documentTextLower.contains("football");

        if (queryWantsSports && !queryWantsTechCoding) {
            if (docIsTechCoding && !docIsSports) {
                return true;
            }
        }
        if (queryWantsTechCoding && !queryWantsSports) {
            if (docIsSports && !docIsTechCoding) {
                return true;
            }
        }

        return false;
    }

    private double calculateRelevanceScore(String userQuery, List<String> specificTerms, VectorSearchResult chunk) {
        String docText = chunk.getText() != null ? chunk.getText().toLowerCase() : "";
        Map<String, String> meta = chunk.getMetadata() != null ? chunk.getMetadata() : Map.of();
        String title = meta.getOrDefault("title", "").toLowerCase();
        String category = meta.getOrDefault("category", meta.getOrDefault("sourceType", "")).toLowerCase();
        String fullDocContent = title + " " + category + " " + docText;

        String qLower = userQuery.toLowerCase();

        // 1. Contradiction Check
        if (isConceptContradicted(qLower, specificTerms, fullDocContent)) {
            return 0.0;
        }

        // 2. If query has NO specific terms (e.g. "upcoming campus events", "all clubs")
        if (specificTerms.isEmpty()) {
            return chunk.getScore() > 0 ? chunk.getScore() : 0.85;
        }

        // 3. Query has specific concept terms. Calculate concept coverage!
        int matchedTerms = 0;
        for (String term : specificTerms) {
            if (containsTermOrSynonym(fullDocContent, term)) {
                matchedTerms++;
            }
        }

        double coverageRatio = (double) matchedTerms / specificTerms.size();

        // If 0 specific terms matched, this document is irrelevant
        if (matchedTerms == 0) {
            return 0.0;
        }

        // Baseline score calculation
        double calculatedScore;
        boolean titleMatch = specificTerms.stream().anyMatch(t -> containsTermOrSynonym(title, t));
        double titleBonus = titleMatch ? 0.10 : 0.0;

        if (chunk.getScore() > 0 && chunk.getScore() < 1.0) {
            // Qdrant Vector Match: combines vector cosine similarity with term coverage ratio & title bonus
            calculatedScore = (chunk.getScore() * (0.60 + 0.40 * coverageRatio)) + titleBonus;
        } else {
            // MySQL Fallback Match: calculated based on term coverage & title bonus
            calculatedScore = 0.55 + (coverageRatio * 0.30) + titleBonus;
        }

        return Math.min(1.0, calculatedScore);
    }

    private boolean containsTermOrSynonym(String text, String term) {
        if (text == null || term == null) {
            return false;
        }
        if (text.contains(term)) {
            return true;
        }
        if (term.endsWith("s") && term.length() > 3 && text.contains(term.substring(0, term.length() - 1))) {
            return true;
        }
        if (!term.endsWith("s") && text.contains(term + "s")) {
            return true;
        }
        List<String> synonyms = CONCEPT_SYNONYMS.get(term);
        if (synonyms != null) {
            for (String syn : synonyms) {
                if (text.contains(syn)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String extractTitleFromContent(String text, String defaultType) {
        if (text == null || text.trim().isEmpty()) {
            return defaultType + " Details";
        }
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.toLowerCase().startsWith("title:") || line.toLowerCase().startsWith("club name:")) {
                return line.substring(line.indexOf(":") + 1).trim();
            }
        }
        return lines[0].replaceAll("^\\[.*?\\]\\s*", "").trim();
    }
}
