package com.resume.resume_api.service;

import com.resume.resume_api.ai.AiRecommendationEngine;
import com.resume.resume_api.dto.AnalysisRequest;
import com.resume.resume_api.dto.AnalysisResponseDTO;
import com.resume.resume_api.entity.*;
import com.resume.resume_api.exception.ResourceNotFoundException;
import com.resume.resume_api.matching.SkillGapAnalysisService;
import com.resume.resume_api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final ResumeRepository resumeRepository;
    private final JobPostingRepository jobPostingRepository;
    private final AnalysisRepository analysisRepository;
    private final AnalysisSkillMatchRepository skillMatchRepository;
    private final AnalysisSkillGapRepository skillGapRepository;
    private final AnalysisRecommendationRepository recommendationRepository;

    private final SkillGapAnalysisService gapAnalysisService;
    private final AiRecommendationEngine recommendationEngine;

    @Transactional
    public AnalysisResponseDTO runAnalysis(AnalysisRequest request) {
        ResumeEntity resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + request.getResumeId()));
        JobPostingEntity job = jobPostingRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found with ID: " + request.getJobId()));

        log.info("Running Resume Intelligence Analysis between Resume '{}' and Job '{}'",
                resume.getCandidateName(), job.getTitle());

        // 1. Perform Multi-dimensional Gap Analysis
        SkillGapAnalysisService.AnalysisResultSummary summary = gapAnalysisService.performGapAnalysis(
                resume, job, job.getSkills(), resume.getSkills()
        );

        // 2. Build Analysis Entity
        AnalysisEntity analysis = AnalysisEntity.builder()
                .resume(resume)
                .jobPosting(job)
                .overallMatchScore(summary.getOverallScore())
                .requiredSkillCoverage(summary.getRequiredCoverage())
                .preferredSkillCoverage(summary.getPreferredCoverage())
                .semanticSimilarityScore(summary.getSemanticScore())
                .experienceAlignmentScore(summary.getExperienceAlignment())
                .educationAlignmentScore(summary.getEducationAlignment())
                .summary(summary.getExecutiveSummary())
                .status("COMPLETED")
                .build();

        // 3. Attach Matches
        for (AnalysisSkillMatchEntity m : summary.getMatches()) {
            m.setAnalysis(analysis);
            analysis.getSkillMatches().add(m);
        }

        // 4. Attach Gaps
        for (AnalysisSkillGapEntity g : summary.getGaps()) {
            g.setAnalysis(analysis);
            analysis.getSkillGaps().add(g);
        }

        // 5. Generate Grounded AI Recommendations via RAG
        List<AiRecommendationEngine.RecommendationItem> recItems =
                recommendationEngine.generateRecommendations(resume, job, summary.getMatches(), summary.getGaps());

        for (AiRecommendationEngine.RecommendationItem r : recItems) {
            AnalysisRecommendationEntity recEntity = AnalysisRecommendationEntity.builder()
                    .analysis(analysis)
                    .category(r.getCategory())
                    .title(r.getTitle())
                    .description(r.getDescription())
                    .priority(r.getPriority())
                    .actionableSteps(r.getActionableSteps())
                    .build();
            analysis.getRecommendations().add(recEntity);
        }

        AnalysisEntity saved = analysisRepository.save(analysis);
        log.info("Analysis completed and persisted. ID: {}, Overall Score: {}%",
                saved.getId(), saved.getOverallMatchScore());

        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<AnalysisResponseDTO> getAllAnalyses() {
        return analysisRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AnalysisResponseDTO getAnalysisById(UUID id) {
        AnalysisEntity analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with ID: " + id));
        return mapToDTO(analysis);
    }

    @Transactional(readOnly = true)
    public List<AnalysisResponseDTO.SkillMatchDTO> getAnalysisSkills(UUID id) {
        AnalysisEntity analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with ID: " + id));
        return analysis.getSkillMatches().stream()
                .map(this::mapMatchToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnalysisResponseDTO.SkillGapDTO> getAnalysisGaps(UUID id) {
        AnalysisEntity analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with ID: " + id));
        return analysis.getSkillGaps().stream()
                .map(this::mapGapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnalysisResponseDTO.RecommendationDTO> getAnalysisRecommendations(UUID id) {
        AnalysisEntity analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with ID: " + id));
        return analysis.getRecommendations().stream()
                .map(this::mapRecToDTO)
                .collect(Collectors.toList());
    }

    private AnalysisResponseDTO mapToDTO(AnalysisEntity a) {
        return AnalysisResponseDTO.builder()
                .id(a.getId())
                .resumeId(a.getResume().getId())
                .resumeFilename(a.getResume().getFilename())
                .candidateName(a.getResume().getCandidateName())
                .jobId(a.getJobPosting().getId())
                .jobTitle(a.getJobPosting().getTitle())
                .jobCompany(a.getJobPosting().getCompany())
                .overallMatchScore(a.getOverallMatchScore())
                .requiredSkillCoverage(a.getRequiredSkillCoverage())
                .preferredSkillCoverage(a.getPreferredSkillCoverage())
                .semanticSimilarityScore(a.getSemanticSimilarityScore())
                .experienceAlignmentScore(a.getExperienceAlignmentScore())
                .educationAlignmentScore(a.getEducationAlignmentScore())
                .summary(a.getSummary())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .matches(a.getSkillMatches().stream().map(this::mapMatchToDTO).collect(Collectors.toList()))
                .gaps(a.getSkillGaps().stream().map(this::mapGapToDTO).collect(Collectors.toList()))
                .recommendations(a.getRecommendations().stream().map(this::mapRecToDTO).collect(Collectors.toList()))
                .build();
    }

    private AnalysisResponseDTO.SkillMatchDTO mapMatchToDTO(AnalysisSkillMatchEntity m) {
        return AnalysisResponseDTO.SkillMatchDTO.builder()
                .id(m.getId())
                .resumeSkillName(m.getResumeSkillName())
                .jobSkillName(m.getJobSkillName())
                .canonicalName(m.getCanonicalName())
                .matchType(m.getMatchType())
                .similarityScore(m.getSimilarityScore())
                .isRequired(m.getIsRequired())
                .explanation(m.getExplanation())
                .build();
    }

    private AnalysisResponseDTO.SkillGapDTO mapGapToDTO(AnalysisSkillGapEntity g) {
        return AnalysisResponseDTO.SkillGapDTO.builder()
                .id(g.getId())
                .jobSkillName(g.getJobSkillName())
                .canonicalName(g.getCanonicalName())
                .isRequired(g.getIsRequired())
                .gapSeverity(g.getGapSeverity())
                .relatedEvidence(g.getRelatedEvidence())
                .explanation(g.getExplanation())
                .build();
    }

    private AnalysisResponseDTO.RecommendationDTO mapRecToDTO(AnalysisRecommendationEntity r) {
        return AnalysisResponseDTO.RecommendationDTO.builder()
                .id(r.getId())
                .category(r.getCategory())
                .title(r.getTitle())
                .description(r.getDescription())
                .priority(r.getPriority())
                .actionableSteps(r.getActionableSteps())
                .build();
    }
}
