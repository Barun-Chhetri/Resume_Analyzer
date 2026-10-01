package com.resume.resume_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchitectureInfoDTO {
    private String applicationName;
    private String version;
    private String javaVersion;
    private String springBootVersion;
    private String activeEmbeddingModel;
    private int vectorDimensions;
    private String databaseDialect;
    private boolean pgvectorEnabled;
    private boolean localFallbackEnabled;
    private List<String> supportedDocumentFormats;
    private List<String> ragPipelineStages;
    private Map<String, Object> systemMetrics;
}
