package com.resume.resume_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "canonical_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CanonicalSkillEntity {

    @Id
    private UUID id;

    @Column(name = "canonical_name", nullable = false, unique = true)
    private String canonicalName;

    @Column(name = "normalized_name", nullable = false, unique = true)
    private String normalizedName;

    @Column(nullable = false)
    private String category; // Languages, Frameworks, Databases, Cloud & DevOps, AI/ML, Architecture, Testing

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "embedding_json", columnDefinition = "TEXT")
    private String embeddingJson;

    @Builder.Default
    @OneToMany(mappedBy = "canonicalSkill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SkillAliasEntity> aliases = new ArrayList<>();

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
