package com.resume.resume_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeDetailDTO {
    private UUID id;
    private String filename;
    private String candidateName;
    private String email;
    private String phone;
    private String location;
    private String summary;
    private Long fileSize;
    private String contentType;
    private String rawText;
    private String parsedJson;
    private List<ExtractedSkillDTO> skills;
    private Map<String, String> sections;
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExtractedSkillDTO {
        private UUID id;
        private String rawName;
        private String canonicalName;
        private String category;
        private String contextSnippet;
        private Double confidence;
    }
}
