package com.resume.resume_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "resume_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeSkillEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @ToString.Exclude
    private ResumeEntity resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canonical_skill_id")
    private CanonicalSkillEntity canonicalSkill;

    @Column(name = "raw_name", nullable = false)
    private String rawName;

    @Column(name = "normalized_name", nullable = false)
    private String normalizedName;

    private String category;

    @Column(name = "context_snippet", columnDefinition = "TEXT")
    private String contextSnippet;

    @Builder.Default
    private Double confidence = 1.0;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
