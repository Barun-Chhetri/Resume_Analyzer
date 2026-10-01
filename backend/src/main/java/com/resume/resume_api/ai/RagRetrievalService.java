package com.resume.resume_api.ai;

import com.resume.resume_api.entity.CanonicalSkillEntity;
import com.resume.resume_api.entity.ResumeSectionEntity;
import com.resume.resume_api.entity.ResumeSkillEntity;
import com.resume.resume_api.repository.CanonicalSkillRepository;
import com.resume.resume_api.repository.ResumeSectionRepository;
import com.resume.resume_api.repository.ResumeSkillRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagRetrievalService {

    private final EmbeddingService embeddingService;
    private final ResumeSectionRepository resumeSectionRepo;
    private final ResumeSkillRepository resumeSkillRepo;
    private final CanonicalSkillRepository canonicalSkillRepo;

    @Data
    @Builder
    public static class RetrievedDocument {
        private String id;
        private String type; // SECTION, SKILL, EXPERIENCE, JOB_REQUIREMENT
        private String content;
        private double similarityScore;
        private Map<String, Object> metadata;
    }

    /**
     * Retrieves top-K relevant resume sections matching a query (e.g. specific job requirement)
     * using cosine similarity over embedded document chunks.
     */
    public List<RetrievedDocument> retrieveResumeContext(UUID resumeId, String query, int topK) {
        if (resumeId == null || query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        double[] queryEmbedding = embeddingService.generateEmbedding(query);
        List<ResumeSectionEntity> sections = resumeSectionRepo.findByResumeId(resumeId);

        List<RetrievedDocument> scored = new ArrayList<>();
        for (ResumeSectionEntity sec : sections) {
            double[] secEmbedding;
            if (sec.getEmbeddingJson() != null && !sec.getEmbeddingJson().isBlank()) {
                secEmbedding = embeddingService.jsonToVector(sec.getEmbeddingJson());
            } else {
                secEmbedding = embeddingService.generateEmbedding(sec.getContent());
            }

            double similarity = embeddingService.cosineSimilarity(queryEmbedding, secEmbedding);
            Map<String, Object> meta = new HashMap<>();
            meta.put("sectionType", sec.getSectionType());

            scored.add(RetrievedDocument.builder()
                    .id(sec.getId().toString())
                    .type("RESUME_SECTION")
                    .content(sec.getContent())
                    .similarityScore(similarity)
                    .metadata(meta)
                    .build());
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble(RetrievedDocument::getSimilarityScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves resume skills semantically relevant to a query or job requirement.
     */
    public List<RetrievedDocument> retrieveRelevantSkills(UUID resumeId, String query, int topK) {
        if (resumeId == null || query == null) {
            return Collections.emptyList();
        }
        double[] queryEmbedding = embeddingService.generateEmbedding(query);
        List<ResumeSkillEntity> skills = resumeSkillRepo.findByResumeId(resumeId);

        List<RetrievedDocument> scored = new ArrayList<>();
        for (ResumeSkillEntity rSkill : skills) {
            double[] skillEmbedding = embeddingService.generateEmbedding(rSkill.getRawName() + " " + rSkill.getCategory());
            double similarity = embeddingService.cosineSimilarity(queryEmbedding, skillEmbedding);

            Map<String, Object> meta = new HashMap<>();
            meta.put("canonicalName", rSkill.getCanonicalSkill() != null ? rSkill.getCanonicalSkill().getCanonicalName() : rSkill.getRawName());
            meta.put("category", rSkill.getCategory());
            meta.put("contextSnippet", rSkill.getContextSnippet());

            scored.add(RetrievedDocument.builder()
                    .id(rSkill.getId().toString())
                    .type("RESUME_SKILL")
                    .content(rSkill.getRawName())
                    .similarityScore(similarity)
                    .metadata(meta)
                    .build());
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble(RetrievedDocument::getSimilarityScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves canonical skills that are semantically similar to a given skill name above a threshold.
     */
    public List<RetrievedDocument> retrieveSimilarSkills(String skillName, double threshold) {
        if (skillName == null || skillName.isBlank()) {
            return Collections.emptyList();
        }
        double[] targetEmbedding = embeddingService.generateEmbedding(skillName);
        List<CanonicalSkillEntity> allCanonical = canonicalSkillRepo.findAll();

        List<RetrievedDocument> matches = new ArrayList<>();
        for (CanonicalSkillEntity c : allCanonical) {
            double[] cEmbedding = embeddingService.generateEmbedding(c.getCanonicalName() + " " + c.getDescription());
            double similarity = embeddingService.cosineSimilarity(targetEmbedding, cEmbedding);

            if (similarity >= threshold) {
                Map<String, Object> meta = new HashMap<>();
                meta.put("category", c.getCategory());
                meta.put("normalizedName", c.getNormalizedName());

                matches.add(RetrievedDocument.builder()
                        .id(c.getId().toString())
                        .type("CANONICAL_SKILL")
                        .content(c.getCanonicalName())
                        .similarityScore(similarity)
                        .metadata(meta)
                        .build());
            }
        }

        return matches.stream()
                .sorted(Comparator.comparingDouble(RetrievedDocument::getSimilarityScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves relevant experience evidence matching a specific job requirement.
     */
    public List<RetrievedDocument> retrieveRelevantExperience(UUID resumeId, String requirement) {
        List<ResumeSectionEntity> expSections = resumeSectionRepo.findByResumeIdAndSectionType(resumeId, "EXPERIENCE");
        if (expSections.isEmpty()) {
            return retrieveResumeContext(resumeId, requirement, 3);
        }

        double[] reqEmbedding = embeddingService.generateEmbedding(requirement);
        List<RetrievedDocument> results = new ArrayList<>();

        for (ResumeSectionEntity exp : expSections) {
            // Split long experience section into individual bullet points/paragraphs for granular retrieval
            String[] paragraphs = exp.getContent().split("\\n\\n|(?<=\\.)\\s+(?=[A-Z•\\-])");
            for (String p : paragraphs) {
                String clean = p.trim();
                if (clean.length() < 15) continue;

                double[] pEmbedding = embeddingService.generateEmbedding(clean);
                double sim = embeddingService.cosineSimilarity(reqEmbedding, pEmbedding);

                Map<String, Object> meta = new HashMap<>();
                meta.put("sectionType", "EXPERIENCE_BULLET");

                results.add(RetrievedDocument.builder()
                        .id(UUID.randomUUID().toString())
                        .type("EXPERIENCE_EVIDENCE")
                        .content(clean)
                        .similarityScore(sim)
                        .metadata(meta)
                        .build());
            }
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(RetrievedDocument::getSimilarityScore).reversed())
                .limit(3)
                .collect(Collectors.toList());
    }
}
