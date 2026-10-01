package com.resume.resume_api.matching;

import com.resume.resume_api.ai.EmbeddingService;
import com.resume.resume_api.entity.JobSkillEntity;
import com.resume.resume_api.entity.ResumeSkillEntity;
import com.resume.resume_api.normalization.SkillNormalizationService;
import com.resume.resume_api.normalization.TextNormalizer;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticMatchingService {

    private final SkillNormalizationService normalizationService;
    private final EmbeddingService embeddingService;

    @Value("${app.ai.semantic-match-threshold:0.65}")
    private double semanticMatchThreshold;

    @Value("${app.ai.related-skill-threshold:0.50}")
    private double relatedSkillThreshold;

    @Data
    @Builder
    public static class MatchEvaluation {
        private boolean isMatched;
        private boolean isRelated;
        private String matchType; // EXACT, NORMALIZED, ALIAS, SEMANTIC_EMBEDDING, NONE
        private double similarityScore;
        private String explanation;
        private ResumeSkillEntity matchedResumeSkill;
        private JobSkillEntity jobSkill;
    }

    /**
     * Evaluates a single Job Skill requirement against all extracted Resume Skills
     * combining exact string equality, normalized canonical comparison, alias mapping,
     * and vector embedding cosine similarity.
     */
    public MatchEvaluation evaluateSkill(JobSkillEntity jobSkill, List<ResumeSkillEntity> resumeSkills) {
        String jobRaw = jobSkill.getRawName();
        String jobNorm = TextNormalizer.normalize(jobRaw);
        SkillNormalizationService.NormalizedSkillResult jobNormalized = normalizationService.normalizeSkill(jobRaw);

        double bestScore = 0.0;
        ResumeSkillEntity bestMatchResumeSkill = null;
        String bestMatchType = "NONE";
        String bestExplanation = "";

        // 1. Check exact and normalized match across resume skills
        for (ResumeSkillEntity rSkill : resumeSkills) {
            String resRaw = rSkill.getRawName();
            String resNorm = TextNormalizer.normalize(resRaw);

            // Exact match
            if (jobRaw.equalsIgnoreCase(resRaw)) {
                return MatchEvaluation.builder()
                        .isMatched(true)
                        .isRelated(false)
                        .matchType("EXACT")
                        .similarityScore(1.0)
                        .explanation(String.format("Exact match found: candidate lists '%s' on resume.", resRaw))
                        .matchedResumeSkill(rSkill)
                        .jobSkill(jobSkill)
                        .build();
            }

            // Normalized match (e.g. "Spring Boot" vs "springboot")
            if (jobNorm.equals(resNorm)) {
                return MatchEvaluation.builder()
                        .isMatched(true)
                        .isRelated(false)
                        .matchType("NORMALIZED")
                        .similarityScore(0.98)
                        .explanation(String.format("Normalized match: '%s' directly equates to '%s'.", resRaw, jobRaw))
                        .matchedResumeSkill(rSkill)
                        .jobSkill(jobSkill)
                        .build();
            }

            // Canonical & Alias Match (e.g. "AWS" vs "Amazon Web Services")
            SkillNormalizationService.NormalizedSkillResult resNormalized = normalizationService.normalizeSkill(resRaw);
            if (jobNormalized.isKnown() && resNormalized.isKnown() &&
                    jobNormalized.getCanonicalName().equalsIgnoreCase(resNormalized.getCanonicalName())) {
                return MatchEvaluation.builder()
                        .isMatched(true)
                        .isRelated(false)
                        .matchType("ALIAS")
                        .similarityScore(0.95)
                        .explanation(String.format("Alias match: '%s' maps to canonical skill '%s'.", resRaw, jobNormalized.getCanonicalName()))
                        .matchedResumeSkill(rSkill)
                        .jobSkill(jobSkill)
                        .build();
            }

            // Vector embedding semantic similarity
            double[] jobVec = embeddingService.generateEmbedding(jobRaw);
            double[] resVec = embeddingService.generateEmbedding(resRaw);
            double sim = embeddingService.cosineSimilarity(jobVec, resVec);

            if (sim > bestScore) {
                bestScore = sim;
                bestMatchResumeSkill = rSkill;

                if (sim >= semanticMatchThreshold) {
                    bestMatchType = "SEMANTIC_EMBEDDING";
                    bestExplanation = String.format("High semantic similarity (%.0f%%) between resume skill '%s' and requirement '%s'.",
                            sim * 100, resRaw, jobRaw);
                } else if (sim >= relatedSkillThreshold) {
                    bestMatchType = "RELATED";
                    bestExplanation = String.format("Partially related (%.0f%% relevance) - '%s' provides relevant adjacent domain experience for '%s'.",
                            sim * 100, resRaw, jobRaw);
                }
            }
        }

        if (bestScore >= semanticMatchThreshold && bestMatchResumeSkill != null) {
            return MatchEvaluation.builder()
                    .isMatched(true)
                    .isRelated(false)
                    .matchType(bestMatchType)
                    .similarityScore(bestScore)
                    .explanation(bestExplanation)
                    .matchedResumeSkill(bestMatchResumeSkill)
                    .jobSkill(jobSkill)
                    .build();
        }

        if (bestScore >= relatedSkillThreshold && bestMatchResumeSkill != null) {
            return MatchEvaluation.builder()
                    .isMatched(false)
                    .isRelated(true)
                    .matchType("RELATED")
                    .similarityScore(bestScore)
                    .explanation(bestExplanation)
                    .matchedResumeSkill(bestMatchResumeSkill)
                    .jobSkill(jobSkill)
                    .build();
        }

        return MatchEvaluation.builder()
                .isMatched(false)
                .isRelated(false)
                .matchType("NONE")
                .similarityScore(bestScore)
                .explanation(String.format("No direct or semantic evidence found on resume for '%s'.", jobRaw))
                .jobSkill(jobSkill)
                .build();
    }
}
