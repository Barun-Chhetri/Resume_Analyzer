package com.resume.resume_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisRequest {
    @NotNull(message = "resumeId is required.")
    private UUID resumeId;

    @NotNull(message = "jobId is required.")
    private UUID jobId;
}
