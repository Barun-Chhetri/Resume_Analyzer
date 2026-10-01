package com.resume.resume_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "analyses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private ResumeEntity resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPostingEntity jobPosting;

    @Column(name = "overall_match_score", nullable = false)
    private Double overallMatchScore;

    @Column(name = "required_skill_coverage", nullable = false)
    private Double requiredSkillCoverage;

    @Column(name = "preferred_skill_coverage", nullable = false)
    private Double preferredSkillCoverage;

    @Column(name = "semantic_similarity_score", nullable = false)
    private Double semanticSimilarityScore;

    @Column(name = "experience_alignment_score", nullable = false)
    private Double experienceAlignmentScore;

    @Column(name = "education_alignment_score", nullable = false)
    private Double educationAlignmentScore;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Builder.Default
    private String status = "COMPLETED";

    @Builder.Default
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnalysisSkillMatchEntity> skillMatches = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnalysisSkillGapEntity> skillGaps = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnalysisRecommendationEntity> recommendations = new ArrayList<>();

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
