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
public class JobPostingDTO {
    private UUID id;
    private String title;
    private String company;
    private String location;
    private String employmentType;
    private String summary;
    private Integer requiredExperienceYears;
    private String requiredDegree;
    private String rawText;
    private String parsedJson;
    private List<JobSkillDTO> skills;
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JobSkillDTO {
        private UUID id;
        private String rawName;
        private String canonicalName;
        private String category;
        private Boolean isRequired;
        private String importance;
    }
}
