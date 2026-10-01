package com.resume.resume_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CanonicalSkillDTO {
    private UUID id;
    private String canonicalName;
    private String normalizedName;
    private String category;
    private String description;
    private List<String> aliases;
}
