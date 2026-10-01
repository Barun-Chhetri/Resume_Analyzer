package com.resume.resume_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "analysis_recommendations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisRecommendationEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    @ToString.Exclude
    private AnalysisEntity analysis;

    @Column(nullable = false)
    private String category; // SKILL_TO_LEARN, RESUME_EMPHASIS, PROJECT_SUGGESTION, BULLET_IMPROVEMENT, INTERVIEW_PREPARATION

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Builder.Default
    private String priority = "HIGH"; // HIGH, MEDIUM, LOW

    @Column(name = "actionable_steps", columnDefinition = "TEXT")
    private String actionableSteps;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
