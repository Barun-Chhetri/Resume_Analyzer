package com.resume.resume_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "skill_aliases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillAliasEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canonical_skill_id", nullable = false)
    @ToString.Exclude
    private CanonicalSkillEntity canonicalSkill;

    @Column(nullable = false)
    private String alias;

    @Column(name = "normalized_alias", nullable = false)
    private String normalizedAlias;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
