package com.resume.resume_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.resume_api.ai.EmbeddingService;
import com.resume.resume_api.ai.LocalDenseEmbeddingModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingServiceTest {

    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        LocalDenseEmbeddingModel model = new LocalDenseEmbeddingModel();
        embeddingService = new EmbeddingService(model, new ObjectMapper());
        ReflectionTestUtils.setField(embeddingService, "providerConfig", "local");
        ReflectionTestUtils.setField(embeddingService, "openAiApiKey", "demo-key");
    }

    @Test
    @DisplayName("Generated embeddings should have exactly 1536 dimensions and unit norm")
    void testEmbeddingDimensionsAndNorm() {
        double[] vec = embeddingService.generateEmbedding("Java Spring Boot Developer");
        assertThat(vec).hasSize(1536);

        double sumSq = 0.0;
        for (double v : vec) sumSq += v * v;
        assertThat(Math.sqrt(sumSq)).isBetween(0.99, 1.01);
    }

    @Test
    @DisplayName("Self-similarity should be 1.0 and identical texts should yield identical vectors")
    void testCosineSimilarityIdentity() {
        double[] vec1 = embeddingService.generateEmbedding("PostgreSQL Database");
        double[] vec2 = embeddingService.generateEmbedding("PostgreSQL Database");

        double sim = embeddingService.cosineSimilarity(vec1, vec2);
        assertThat(sim).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    @DisplayName("Semantically related terms should yield higher cosine similarity than unrelated terms")
    void testSemanticCloseness() {
        double[] javaVec = embeddingService.generateEmbedding("Java backend development with Spring");
        double[] springVec = embeddingService.generateEmbedding("Spring Boot microservice REST API");
        double[] cookingVec = embeddingService.generateEmbedding("Culinary chef baking pastries in kitchen");

        double simRelated = embeddingService.cosineSimilarity(javaVec, springVec);
        double simUnrelated = embeddingService.cosineSimilarity(javaVec, cookingVec);

        assertThat(simRelated).isGreaterThan(simUnrelated);
    }

    @Test
    @DisplayName("Vector JSON serialization and deserialization should preserve vector components")
    void testVectorSerialization() {
        double[] original = embeddingService.generateEmbedding("Kubernetes Docker orchestration");
        String json = embeddingService.vectorToJson(original);
        double[] reconstructed = embeddingService.jsonToVector(json);

        assertThat(reconstructed).hasSize(original.length);
        assertThat(reconstructed[0]).isEqualTo(original[0]);
        assertThat(reconstructed[100]).isEqualTo(original[100]);
    }
}
