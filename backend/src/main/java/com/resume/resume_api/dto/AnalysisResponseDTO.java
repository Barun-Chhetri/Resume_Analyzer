package com.resume.resume_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResponseDTO {
    private UUID id;
    private UUID resumeId;
    private String resumeFilename;
    private String candidateName;
    private UUID jobId;
    private String jobTitle;
    private String jobCompany;

    private Double overallMatchScore;
    private Double requiredSkillCoverage;
    private Double preferredSkillCoverage;
    private Double semanticSimilarityScore;
    private Double experienceAlignmentScore;
    private Double educationAlignmentScore;

    private String summary;
    private String status;
    private Instant createdAt;

    private List<SkillMatchDTO> matches;
    private List<SkillGapDTO> gaps;
    private List<RecommendationDTO> recommendations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SkillMatchDTO {
        private UUID id;
        private String resumeSkillName;
        private String jobSkillName;
        private String canonicalName;
        private String matchType; // EXACT, NORMALIZED, ALIAS, SEMANTIC_EMBEDDING
        private Double similarityScore;
        private Boolean isRequired;
        private String explanation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SkillGapDTO {
        private UUID id;
        private String jobSkillName;
        private String canonicalName;
        private Boolean isRequired;
        private String gapSeverity; // CRITICAL, MODERATE, LOW
        private String relatedEvidence;
        private String explanation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendationDTO {
        private UUID id;
        private String category;
        private String title;
        private String description;
        private String priority;
        private String actionableSteps;
    }
}
