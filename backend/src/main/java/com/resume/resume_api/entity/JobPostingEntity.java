package com.resume.resume_api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "job_postings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostingEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String company;
    private String location;

    @Column(name = "employment_type")
    private String employmentType; // Full-time, Internship, Contract, Remote

    @Column(name = "raw_text", columnDefinition = "TEXT", nullable = false)
    private String rawText;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "required_experience_years")
    @Builder.Default
    private Integer requiredExperienceYears = 0;

    @Column(name = "required_degree")
    private String requiredDegree;

    @Column(name = "parsed_json", columnDefinition = "TEXT")
    private String parsedJson;

    @Builder.Default
    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JobSkillEntity> skills = new ArrayList<>();

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
