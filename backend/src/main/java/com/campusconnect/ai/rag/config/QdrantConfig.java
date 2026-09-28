package com.campusconnect.ai.rag.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for Qdrant Vector Database Integration in the Campus RAG Knowledge Base.
 * Manages Qdrant URL, API key, collection name, host, and port via environment variables.
 * Automatically verifies and initializes collection 'campus_knowledge_base' with 768-dim vectors.
 */
@Configuration
public class QdrantConfig {

    private static final Logger log = LoggerFactory.getLogger(QdrantConfig.class);

    @Value("${qdrant.url:${QDRANT_URL:http://localhost:6334}}")
    private String url;

    @Value("${qdrant.api.key:${QDRANT_API_KEY:}}")
    private String apiKey;

    @Value("${qdrant.collection-name:${QDRANT_COLLECTION_NAME:campus_knowledge_base}}")
    private String collectionName;

    @Value("${qdrant.host:${QDRANT_HOST:localhost}}")
    private String host;

    @Value("${qdrant.port:${QDRANT_PORT:6334}}")
    private int port;

    @Value("${qdrant.use-tls:${QDRANT_USE_TLS:false}}")
    private boolean useTls;

    @Value("${qdrant.enabled:${QDRANT_ENABLED:true}}")
    private boolean enabled;

    public String getUrl() {
        return url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isUseTls() {
        return useTls;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Bean(name = "qdrantClient")
    public QdrantClient qdrantClient() {
        if (!enabled) {
            log.info("Qdrant integration is explicitly disabled.");
            return null;
        }

        String targetHost = (host != null && !host.trim().isEmpty()) ? host.trim() : "localhost";
        int targetPort = port > 0 ? port : 6334;
        boolean targetUseTls = useTls;

        if (url != null && !url.trim().isEmpty() && !"http://localhost:6334".equals(url.trim())) {
            try {
                URI uri = new URI(url.trim());
                if (uri.getHost() != null && !uri.getHost().isEmpty()) {
                    targetHost = uri.getHost();
                }
                if (uri.getPort() != -1) {
                    targetPort = uri.getPort();
                }
                if ("https".equalsIgnoreCase(uri.getScheme())) {
                    targetUseTls = true;
                }
            } catch (Exception e) {
                log.warn("Could not parse QDRANT_URL '{}': {}", url, e.getMessage());
            }
        }

        try {
            QdrantGrpcClient.Builder grpcBuilder = QdrantGrpcClient.newBuilder(targetHost, targetPort, targetUseTls);
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                grpcBuilder.withApiKey(apiKey.trim());
            }

            QdrantClient client = new QdrantClient(grpcBuilder.build());

            // Check & auto-create collection if missing
            String targetCollection = collectionName != null ? collectionName.trim() : "campus_knowledge_base";
            try {
                Boolean exists = client.collectionExistsAsync(targetCollection).get(5, TimeUnit.SECONDS);
                if (Boolean.TRUE.equals(exists)) {
                    log.info("Qdrant collection '{}' verified at {}:{}.", targetCollection, targetHost, targetPort);
                } else {
                    log.info("Creating Qdrant collection '{}' (dim=768, distance=Cosine) at {}:{}...", targetCollection, targetHost, targetPort);
                    client.createCollectionAsync(
                            targetCollection,
                            VectorParams.newBuilder().setSize(768).setDistance(Distance.Cosine).build()
                    ).get(5, TimeUnit.SECONDS);
                    log.info("Successfully created Qdrant collection '{}'.", targetCollection);
                }
            } catch (Exception e) {
                log.warn("Could not verify/create Qdrant collection '{}': {}. Vector store will operate offline.", targetCollection, e.getMessage());
            }

            return client;
        } catch (Exception e) {
            log.warn("Qdrant client initialization failed for {}:{}: {}. Continuing with vector store offline.", targetHost, targetPort, e.getMessage());
            return null;
        }
    }

    @Bean(name = "qdrantEmbeddingStore")
    public EmbeddingStore<TextSegment> qdrantEmbeddingStore(@org.springframework.beans.factory.annotation.Autowired(required = false) QdrantClient client) {
        if (!enabled || client == null) {
            log.info("Qdrant embedding store integration is disabled or offline.");
            return null;
        }

        try {
            String targetCollection = collectionName != null ? collectionName.trim() : "campus_knowledge_base";
            return new QdrantEmbeddingStore(client, targetCollection, "text");
        } catch (Exception e) {
            log.warn("Failed to create QdrantEmbeddingStore: {}. Continuing offline.", e.getMessage());
            return null;
        }
    }
}


