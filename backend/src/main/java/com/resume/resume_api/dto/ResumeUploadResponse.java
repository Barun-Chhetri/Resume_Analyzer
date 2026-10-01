package com.resume.resume_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeUploadResponse {
    private UUID id;
    private String filename;
    private String candidateName;
    private String email;
    private int totalSkillsExtracted;
    private int totalSectionsParsed;
    private String message;
}
