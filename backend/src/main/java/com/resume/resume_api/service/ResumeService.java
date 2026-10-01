package com.resume.resume_api.service;

import com.resume.resume_api.dto.ResumeDTO;
import com.resume.resume_api.dto.ResumeDetailDTO;

import java.util.List;
import java.util.UUID;

public interface ResumeService {
    // Legacy support for user's personal resume portfolio
    ResumeDTO getResume();

    // AI Platform methods
    List<ResumeDetailDTO> getAllResumes();
    ResumeDetailDTO getResumeDetailById(UUID id);
    void deleteResume(UUID id);
    ResumeDetailDTO updateCandidateName(UUID id, String candidateName);
}
