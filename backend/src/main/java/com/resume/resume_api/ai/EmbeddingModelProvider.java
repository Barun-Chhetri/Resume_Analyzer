package com.resume.resume_api.ai;

import java.util.List;

public interface EmbeddingModelProvider {
    /**
     * Name of the embedding model provider (e.g. "openai-text-embedding-3-small", "local-dense-semantic").
     */
    String getProviderName();

    /**
     * Embedding vector dimension (e.g. 1536).
     */
    int getDimension();

    /**
     * Generates a normalized dense vector embedding for single text.
     */
    double[] embed(String text);

    /**
     * Generates normalized dense vector embeddings for a batch of texts.
     */
    List<double[]> embedBatch(List<String> texts);
}
