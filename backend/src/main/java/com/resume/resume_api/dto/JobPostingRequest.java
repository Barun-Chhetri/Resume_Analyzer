package com.resume.resume_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostingRequest {
    private String title;
    private String company;
    private String location;
    private String employmentType;

    @NotBlank(message = "Job description raw text cannot be blank.")
    private String rawText;
}
