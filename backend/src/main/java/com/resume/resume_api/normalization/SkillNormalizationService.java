package com.resume.resume_api.normalization;

import com.resume.resume_api.entity.CanonicalSkillEntity;
import com.resume.resume_api.entity.SkillAliasEntity;
import com.resume.resume_api.repository.CanonicalSkillRepository;
import com.resume.resume_api.repository.SkillAliasRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillNormalizationService {

    private final CanonicalSkillRepository canonicalSkillRepo;
    private final SkillAliasRepository skillAliasRepo;

    @Data
    @Builder
    public static class NormalizedSkillResult {
        private String originalName;
        private String canonicalName;
        private String normalizedName;
        private String category;
        private UUID canonicalId;
        private boolean isKnown;
        private String matchType; // "CANONICAL", "ALIAS", "SYNONYM", "UNRECOGNIZED"
    }

    // High performance memory cache for fast normalization lookups
    private final Map<String, NormalizedSkillResult> aliasCache = new ConcurrentHashMap<>();

    // Built-in static catalog for rapid initialization and offline test resilience
    private static final Map<String, String[]> BUILT_IN_TAXONOMY = new LinkedHashMap<>();

    static {
        BUILT_IN_TAXONOMY.put("Java", new String[]{"java", "core java", "java 21", "java 17", "java 11", "java 8", "java se", "java ee", "j2ee"});
        BUILT_IN_TAXONOMY.put("Python", new String[]{"python", "python 3", "python3", "py"});
        BUILT_IN_TAXONOMY.put("JavaScript", new String[]{"javascript", "js", "es6", "es2020", "es2022", "vanilla js"});
        BUILT_IN_TAXONOMY.put("TypeScript", new String[]{"typescript", "ts"});
        BUILT_IN_TAXONOMY.put("Spring Boot", new String[]{"spring", "spring boot", "spring framework", "springboot", "spring mvc", "spring data jpa"});
        BUILT_IN_TAXONOMY.put("Spring AI", new String[]{"spring ai", "springai", "spring-ai"});
        BUILT_IN_TAXONOMY.put("PostgreSQL", new String[]{"postgres", "postgresql", "postgres sql", "psql"});
        BUILT_IN_TAXONOMY.put("PGVector", new String[]{"pgvector", "pg vector", "pg-vector", "postgres vector"});
        BUILT_IN_TAXONOMY.put("Amazon Web Services", new String[]{"aws", "amazon web services", "amazon aws", "ec2", "s3", "lambda", "ecs", "eks"});
        BUILT_IN_TAXONOMY.put("Microsoft Azure", new String[]{"azure", "microsoft azure", "ms azure"});
        BUILT_IN_TAXONOMY.put("Google Cloud Platform", new String[]{"gcp", "google cloud", "google cloud platform"});
        BUILT_IN_TAXONOMY.put("Docker", new String[]{"docker", "docker compose", "containerization", "containers"});
        BUILT_IN_TAXONOMY.put("Kubernetes", new String[]{"kubernetes", "k8s"});
        BUILT_IN_TAXONOMY.put("Machine Learning", new String[]{"machine learning", "ml", "scikit-learn"});
        BUILT_IN_TAXONOMY.put("Artificial Intelligence", new String[]{"artificial intelligence", "ai", "genai", "generative ai"});
        BUILT_IN_TAXONOMY.put("Retrieval-Augmented Generation", new String[]{"rag", "retrieval augmented generation", "retrieval-augmented generation"});
        BUILT_IN_TAXONOMY.put("React", new String[]{"react", "reactjs", "react.js", "react 18"});
        BUILT_IN_TAXONOMY.put("Next.js", new String[]{"next.js", "nextjs", "next"});
        BUILT_IN_TAXONOMY.put("Node.js", new String[]{"node", "nodejs", "node.js"});
        BUILT_IN_TAXONOMY.put("Express.js", new String[]{"express", "expressjs", "express.js"});
        BUILT_IN_TAXONOMY.put("CI/CD", new String[]{"ci/cd", "cicd", "continuous integration", "github actions", "jenkins"});
        BUILT_IN_TAXONOMY.put("Git", new String[]{"git", "github", "gitlab", "version control"});
        BUILT_IN_TAXONOMY.put("REST API", new String[]{"rest", "restful", "rest api", "rest apis", "restful api", "restful web services"});
        BUILT_IN_TAXONOMY.put("Microservices", new String[]{"microservices", "microservice", "micro-services"});
        BUILT_IN_TAXONOMY.put("GraphQL", new String[]{"graphql", "gql"});
        BUILT_IN_TAXONOMY.put("Redis", new String[]{"redis", "redis cache"});
        BUILT_IN_TAXONOMY.put("MongoDB", new String[]{"mongodb", "mongo", "nosql"});
        BUILT_IN_TAXONOMY.put("SQL", new String[]{"sql", "relational database", "rdbms"});
        BUILT_IN_TAXONOMY.put("Linux", new String[]{"linux", "ubuntu", "centos", "bash", "shell scripting"});
        BUILT_IN_TAXONOMY.put("Terraform", new String[]{"terraform", "iac", "infrastructure as code"});
        BUILT_IN_TAXONOMY.put("Unit Testing", new String[]{"unit testing", "junit", "junit 5", "unit test", "tdd"});
        BUILT_IN_TAXONOMY.put("Mockito", new String[]{"mockito", "mocking"});
        BUILT_IN_TAXONOMY.put("HTML/CSS", new String[]{"html", "css", "html5", "css3"});
        BUILT_IN_TAXONOMY.put("Bootstrap", new String[]{"bootstrap", "bootstrap 5"});
        BUILT_IN_TAXONOMY.put("Tailwind CSS", new String[]{"tailwind", "tailwindcss"});
        BUILT_IN_TAXONOMY.put("Natural Language Processing", new String[]{"nlp", "natural language processing"});
        BUILT_IN_TAXONOMY.put("Embeddings", new String[]{"embeddings", "vector embeddings", "text embeddings"});
        BUILT_IN_TAXONOMY.put("Vector Databases", new String[]{"vector db", "vector databases", "vector database", "pinecone", "chromadb", "milvus"});
    }

    @PostConstruct
    public void init() {
        populateBuiltIns();
        try {
            refreshFromDatabase();
        } catch (Exception e) {
            log.warn("Could not load skills from DB at startup (will retry on demand): {}", e.getMessage());
        }
    }

    private void populateBuiltIns() {
        for (Map.Entry<String, String[]> entry : BUILT_IN_TAXONOMY.entrySet()) {
            String canonical = entry.getKey();
            String normCanonical = TextNormalizer.normalize(canonical);
            String category = determineCategory(canonical);

            NormalizedSkillResult base = NormalizedSkillResult.builder()
                    .originalName(canonical)
                    .canonicalName(canonical)
                    .normalizedName(normCanonical)
                    .category(category)
                    .isKnown(true)
                    .matchType("CANONICAL")
                    .build();

            aliasCache.put(normCanonical, base);

            for (String alias : entry.getValue()) {
                String normAlias = TextNormalizer.normalize(alias);
                NormalizedSkillResult aliasResult = NormalizedSkillResult.builder()
                        .originalName(alias)
                        .canonicalName(canonical)
                        .normalizedName(normCanonical)
                        .category(category)
                        .isKnown(true)
                        .matchType("ALIAS")
                        .build();
                aliasCache.put(normAlias, aliasResult);
            }
        }
    }

    public void refreshFromDatabase() {
        try {
            List<CanonicalSkillEntity> skills = canonicalSkillRepo.findAll();
            for (CanonicalSkillEntity skill : skills) {
                NormalizedSkillResult res = NormalizedSkillResult.builder()
                        .originalName(skill.getCanonicalName())
                        .canonicalName(skill.getCanonicalName())
                        .normalizedName(skill.getNormalizedName())
                        .category(skill.getCategory())
                        .canonicalId(skill.getId())
                        .isKnown(true)
                        .matchType("CANONICAL")
                        .build();
                aliasCache.put(skill.getNormalizedName(), res);

                if (skill.getAliases() != null) {
                    for (SkillAliasEntity alias : skill.getAliases()) {
                        NormalizedSkillResult aliasRes = NormalizedSkillResult.builder()
                                .originalName(alias.getAlias())
                                .canonicalName(skill.getCanonicalName())
                                .normalizedName(skill.getNormalizedName())
                                .category(skill.getCategory())
                                .canonicalId(skill.getId())
                                .isKnown(true)
                                .matchType("ALIAS")
                                .build();
                        aliasCache.put(alias.getNormalizedAlias(), aliasRes);
                    }
                }
            }
            log.info("Loaded {} skill normalization mappings into cache", aliasCache.size());
        } catch (Exception e) {
            log.warn("Error refreshing skills from database: {}", e.getMessage());
        }
    }

    /**
     * Normalizes a raw skill string to its canonical entity representation.
     */
    public NormalizedSkillResult normalizeSkill(String rawSkill) {
        if (rawSkill == null || rawSkill.trim().isEmpty()) {
            return NormalizedSkillResult.builder()
                    .originalName("")
                    .canonicalName("Unknown")
                    .normalizedName("")
                    .category("Other")
                    .isKnown(false)
                    .matchType("EMPTY")
                    .build();
        }

        String cleaned = rawSkill.trim();
        String normKey = TextNormalizer.normalize(cleaned);

        // 1. Direct cache lookup
        NormalizedSkillResult cached = aliasCache.get(normKey);
        if (cached != null) {
            return NormalizedSkillResult.builder()
                    .originalName(cleaned)
                    .canonicalName(cached.getCanonicalName())
                    .normalizedName(cached.getNormalizedName())
                    .category(cached.getCategory())
                    .canonicalId(cached.getCanonicalId())
                    .isKnown(true)
                    .matchType(cached.getMatchType())
                    .build();
        }

        // 2. Database Alias Lookup
        try {
            Optional<SkillAliasEntity> dbAlias = skillAliasRepo.findByNormalizedAlias(normKey);
            if (dbAlias.isPresent()) {
                CanonicalSkillEntity parent = dbAlias.get().getCanonicalSkill();
                NormalizedSkillResult res = NormalizedSkillResult.builder()
                        .originalName(cleaned)
                        .canonicalName(parent.getCanonicalName())
                        .normalizedName(parent.getNormalizedName())
                        .category(parent.getCategory())
                        .canonicalId(parent.getId())
                        .isKnown(true)
                        .matchType("DB_ALIAS")
                        .build();
                aliasCache.put(normKey, res);
                return res;
            }

            // 3. Database Canonical Lookup
            Optional<CanonicalSkillEntity> dbSkill = canonicalSkillRepo.findByNormalizedName(normKey);
            if (dbSkill.isPresent()) {
                CanonicalSkillEntity skill = dbSkill.get();
                NormalizedSkillResult res = NormalizedSkillResult.builder()
                        .originalName(cleaned)
                        .canonicalName(skill.getCanonicalName())
                        .normalizedName(skill.getNormalizedName())
                        .category(skill.getCategory())
                        .canonicalId(skill.getId())
                        .isKnown(true)
                        .matchType("DB_CANONICAL")
                        .build();
                aliasCache.put(normKey, res);
                return res;
            }
        } catch (Exception e) {
            log.trace("DB skill lookup skipped: {}", e.getMessage());
        }

        // 4. Fallback for unrecognized skills: format properly
        String formatted = formatCapitalization(cleaned);
        return NormalizedSkillResult.builder()
                .originalName(cleaned)
                .canonicalName(formatted)
                .normalizedName(normKey)
                .category("Technical Skills")
                .isKnown(false)
                .matchType("UNRECOGNIZED")
                .build();
    }

    private String formatCapitalization(String text) {
        if (text.length() <= 3) {
            return text.toUpperCase(Locale.ROOT);
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    public static String determineCategory(String skill) {
        String lower = skill.toLowerCase(Locale.ROOT);
        if (lower.contains("java") || lower.contains("python") || lower.contains("c++") || lower.contains("c#")
                || lower.contains("rust") || lower.contains("golang") || lower.contains("typescript")
                || lower.contains("javascript") || lower.contains("html") || lower.contains("sql")) {
            return "Languages";
        }
        if (lower.contains("spring") || lower.contains("react") || lower.contains("angular") || lower.contains("vue")
                || lower.contains("express") || lower.contains("django") || lower.contains("fastapi")
                || lower.contains("next")) {
            return "Frameworks";
        }
        if (lower.contains("postgres") || lower.contains("mysql") || lower.contains("mongo") || lower.contains("redis")
                || lower.contains("vector") || lower.contains("database")) {
            return "Databases";
        }
        if (lower.contains("aws") || lower.contains("azure") || lower.contains("cloud") || lower.contains("docker")
                || lower.contains("kubernetes") || lower.contains("ci/cd") || lower.contains("linux")
                || lower.contains("terraform")) {
            return "Cloud & DevOps";
        }
        if (lower.contains("ai") || lower.contains("ml") || lower.contains("rag") || lower.contains("nlp")
                || lower.contains("embedding") || lower.contains("learning")) {
            return "AI/ML";
        }
        if (lower.contains("rest") || lower.contains("microservice") || lower.contains("graphql") || lower.contains("kafka")) {
            return "Architecture";
        }
        if (lower.contains("test") || lower.contains("junit") || lower.contains("mockito")) {
            return "Testing";
        }
        return "Technical Skills";
    }
}
