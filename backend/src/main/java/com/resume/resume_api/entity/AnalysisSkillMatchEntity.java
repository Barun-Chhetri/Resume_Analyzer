package com.resume.resume_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "analysis_skill_matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisSkillMatchEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    @ToString.Exclude
    private AnalysisEntity analysis;

    @Column(name = "resume_skill_name", nullable = false)
    private String resumeSkillName;

    @Column(name = "job_skill_name", nullable = false)
    private String jobSkillName;

    @Column(name = "canonical_name")
    private String canonicalName;

    @Column(name = "match_type", nullable = false)
    private String matchType; // EXACT, NORMALIZED, ALIAS, SEMANTIC_EMBEDDING

    @Column(name = "similarity_score", nullable = false)
    private Double similarityScore;

    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = true;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
