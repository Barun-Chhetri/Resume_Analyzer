package com.resume.resume_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "job_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSkillEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @ToString.Exclude
    private JobPostingEntity jobPosting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canonical_skill_id")
    private CanonicalSkillEntity canonicalSkill;

    @Column(name = "raw_name", nullable = false)
    private String rawName;

    @Column(name = "normalized_name", nullable = false)
    private String normalizedName;

    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = true;

    @Builder.Default
    private String importance = "HIGH"; // CRITICAL, HIGH, MEDIUM, PREFERRED

    private String category;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
