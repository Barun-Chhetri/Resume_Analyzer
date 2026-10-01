package com.resume.resume_api.ai;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LocalDenseEmbeddingModel implements EmbeddingModelProvider {

    private static final int DIMENSION = 1536;

    // Semantic cluster basis vectors mapped into designated latent coordinate bands
    private static final Map<String, Integer> DOMAIN_BANDS = new HashMap<>();

    static {
        DOMAIN_BANDS.put("backend", 0);
        DOMAIN_BANDS.put("java", 80);
        DOMAIN_BANDS.put("spring", 160);
        DOMAIN_BANDS.put("database", 240);
        DOMAIN_BANDS.put("sql", 320);
        DOMAIN_BANDS.put("frontend", 400);
        DOMAIN_BANDS.put("javascript", 480);
        DOMAIN_BANDS.put("react", 560);
        DOMAIN_BANDS.put("cloud", 640);
        DOMAIN_BANDS.put("devops", 720);
        DOMAIN_BANDS.put("container", 800);
        DOMAIN_BANDS.put("ai", 880);
        DOMAIN_BANDS.put("ml", 960);
        DOMAIN_BANDS.put("rag", 1040);
        DOMAIN_BANDS.put("vector", 1120);
        DOMAIN_BANDS.put("python", 1200);
        DOMAIN_BANDS.put("testing", 1280);
        DOMAIN_BANDS.put("architecture", 1360);
        DOMAIN_BANDS.put("distributed", 1440);
    }

    @Override
    public String getProviderName() {
        return "local-dense-semantic-1536";
    }

    @Override
    public int getDimension() {
        return DIMENSION;
    }

    @Override
    public double[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new double[DIMENSION];
        }

        double[] vector = new double[DIMENSION];
        String lower = text.toLowerCase(Locale.ROOT).trim();
        String[] tokens = lower.split("[\\s,;:.\\-_/()\\[\\]{}]+");

        // 1. Token-level pseudo-random feature hashing (Murmur-style deterministic projection)
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.isEmpty()) continue;

            int hash1 = tokenHash(token, 0x9747b28c);
            int hash2 = tokenHash(token, 0x5bd1e995);

            for (int k = 0; k < 6; k++) {
                int idx = Math.abs((hash1 + k * hash2) % DIMENSION);
                double sign = ((hash2 >> (k % 16)) & 1) == 0 ? 1.0 : -1.0;
                vector[idx] += sign * (1.0 / Math.sqrt(tokens.length));
            }

            // 2. Character n-gram hashing for subword matching (e.g. "postgres" and "postgresql")
            if (token.length() >= 3) {
                for (int n = 0; n <= token.length() - 3; n++) {
                    String trigram = token.substring(n, n + 3);
                    int triHash = tokenHash(trigram, 0x1b873593);
                    int triIdx = Math.abs(triHash % DIMENSION);
                    vector[triIdx] += 0.35;
                }
            }

            // 3. Domain semantic band activation
            for (Map.Entry<String, Integer> band : DOMAIN_BANDS.entrySet()) {
                String domain = band.getKey();
                int baseIndex = band.getValue();

                if (domainMatches(token, domain)) {
                    for (int offset = 0; offset < 40; offset++) {
                        int pos = (baseIndex + offset) % DIMENSION;
                        double weight = Math.cos((offset * Math.PI) / 40.0) * 1.5;
                        vector[pos] += weight;
                    }
                }
            }
        }

        // 4. L2 Normalize to unit sphere (essential for cosine similarity)
        return normalizeL2(vector);
    }

    @Override
    public List<double[]> embedBatch(List<String> texts) {
        List<double[]> results = new ArrayList<>(texts.size());
        for (String text : texts) {
            results.add(embed(text));
        }
        return results;
    }

    private boolean domainMatches(String token, String domain) {
        if (token.contains(domain)) return true;
        return switch (domain) {
            case "backend" -> token.equals("java") || token.equals("spring") || token.equals("node") || token.equals("api") || token.equals("server");
            case "java" -> token.equals("spring") || token.equals("jvm") || token.equals("j2ee") || token.equals("springboot");
            case "spring" -> token.equals("springboot") || token.equals("springai") || token.equals("jpa") || token.equals("hibernate");
            case "database" -> token.equals("sql") || token.equals("postgres") || token.equals("postgresql") || token.equals("mysql") || token.equals("mongodb") || token.equals("redis");
            case "sql" -> token.equals("postgres") || token.equals("rdbms") || token.equals("query") || token.equals("pgvector");
            case "frontend" -> token.equals("react") || token.equals("js") || token.equals("javascript") || token.equals("typescript") || token.equals("ui") || token.equals("css") || token.equals("html");
            case "react" -> token.equals("nextjs") || token.equals("redux") || token.equals("frontend") || token.equals("jsx");
            case "cloud" -> token.equals("aws") || token.equals("azure") || token.equals("gcp") || token.equals("ec2") || token.equals("s3");
            case "devops" -> token.equals("docker") || token.equals("kubernetes") || token.equals("k8s") || token.equals("cicd") || token.equals("terraform");
            case "container" -> token.equals("docker") || token.equals("kubernetes") || token.equals("k8s") || token.equals("containerization");
            case "ai" -> token.equals("ml") || token.equals("rag") || token.equals("llm") || token.equals("gpt") || token.equals("embedding");
            case "rag" -> token.equals("retrieval") || token.equals("vector") || token.equals("pgvector") || token.equals("embeddings");
            case "vector" -> token.equals("pgvector") || token.equals("similarity") || token.equals("pinecone") || token.equals("dimension");
            default -> false;
        };
    }

    private int tokenHash(String str, int seed) {
        int h = seed;
        for (int i = 0; i < str.length(); i++) {
            h = 31 * h + str.charAt(i);
        }
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }

    private double[] normalizeL2(double[] vec) {
        double sumSq = 0.0;
        for (double v : vec) {
            sumSq += v * v;
        }
        if (sumSq < 1e-9) {
            return vec;
        }
        double norm = Math.sqrt(sumSq);
        double[] normalized = new double[vec.length];
        for (int i = 0; i < vec.length; i++) {
            normalized[i] = vec[i] / norm;
        }
        return normalized;
    }
}
