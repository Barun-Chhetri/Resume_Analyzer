package com.resume.resume_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "analysis_skill_gaps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisSkillGapEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    @ToString.Exclude
    private AnalysisEntity analysis;

    @Column(name = "job_skill_name", nullable = false)
    private String jobSkillName;

    @Column(name = "canonical_name")
    private String canonicalName;

    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = true;

    @Column(name = "gap_severity")
    @Builder.Default
    private String gapSeverity = "CRITICAL"; // CRITICAL, MODERATE, LOW

    @Column(name = "related_evidence", columnDefinition = "TEXT")
    private String relatedEvidence;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
