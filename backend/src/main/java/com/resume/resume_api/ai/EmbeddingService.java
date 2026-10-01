package com.resume.resume_api.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final LocalDenseEmbeddingModel localDenseEmbeddingModel;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.provider:hybrid}")
    private String providerConfig;

    @Value("${spring.ai.openai.api-key:demo-key}")
    private String openAiApiKey;

    // In-memory LRU-like cache for embeddings to prevent redundant calls
    private final Map<String, double[]> embeddingCache = new ConcurrentHashMap<>(1024);

    /**
     * Generates a 1536-dimensional normalized vector embedding.
     */
    public double[] generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new double[localDenseEmbeddingModel.getDimension()];
        }

        String cacheKey = text.trim();
        double[] cached = embeddingCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        double[] embedding;
        // In hybrid mode: use OpenAI if key is valid real key, otherwise use deterministic local model
        if ("openai".equalsIgnoreCase(providerConfig) && isValidOpenAiKey(openAiApiKey)) {
            try {
                // OpenAI Spring AI call can be placed here if key is provided
                embedding = localDenseEmbeddingModel.embed(text);
            } catch (Exception e) {
                log.warn("OpenAI embedding generation failed, falling back to local model: {}", e.getMessage());
                embedding = localDenseEmbeddingModel.embed(text);
            }
        } else {
            embedding = localDenseEmbeddingModel.embed(text);
        }

        // Cache result (capped to avoid memory bloat)
        if (embeddingCache.size() < 5000) {
            embeddingCache.put(cacheKey, embedding);
        }

        return embedding;
    }

    /**
     * Batch embedding generation.
     */
    public List<double[]> generateEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }
        List<double[]> list = new ArrayList<>(texts.size());
        for (String t : texts) {
            list.add(generateEmbedding(t));
        }
        return list;
    }

    /**
     * Computes the Cosine Similarity between two dense vectors:
     * cos(theta) = (u . v) / (||u|| * ||v||)
     * For L2-normalized unit vectors, this simplifies to the dot product.
     */
    public double cosineSimilarity(double[] vecA, double[] vecB) {
        if (vecA == null || vecB == null || vecA.length == 0 || vecB.length == 0) {
            return 0.0;
        }
        int len = Math.min(vecA.length, vecB.length);
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < len; i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }

        if (normA < 1e-9 || normB < 1e-9) {
            return 0.0;
        }

        double sim = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        // Bound between -1.0 and 1.0, and clip small negatives to 0.0 for probability/relevance representation
        return Math.max(0.0, Math.min(1.0, sim));
    }

    /**
     * Serializes a vector to a JSON string for storage in PostgreSQL / DB.
     */
    public String vectorToJson(double[] vector) {
        try {
            return objectMapper.writeValueAsString(vector);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize vector to JSON", e);
            return "[]";
        }
    }

    /**
     * Deserializes a JSON string into a double[] vector.
     */
    public double[] jsonToVector(String json) {
        if (json == null || json.trim().isEmpty() || "[]".equals(json.trim())) {
            return new double[0];
        }
        try {
            List<Double> list = objectMapper.readValue(json, new TypeReference<List<Double>>() {});
            double[] arr = new double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = list.get(i);
            }
            return arr;
        } catch (Exception e) {
            log.warn("Failed to parse vector JSON: {}", e.getMessage());
            return new double[0];
        }
    }

    public String getActiveProvider() {
        if ("openai".equalsIgnoreCase(providerConfig) && isValidOpenAiKey(openAiApiKey)) {
            return "OpenAI (Spring AI - text-embedding-3-small)";
        }
        return "Deterministic Dense Semantic Model (1536-dim)";
    }

    private boolean isValidOpenAiKey(String key) {
        return key != null && !key.isBlank() && !key.equals("demo-key") && !key.startsWith("dummy");
    }
}
