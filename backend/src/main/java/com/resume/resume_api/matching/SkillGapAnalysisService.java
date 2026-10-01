package com.resume.resume_api.matching;

import com.resume.resume_api.entity.*;
import com.resume.resume_api.normalization.SkillNormalizationService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillGapAnalysisService {

    private final SemanticMatchingService matchingService;
    private final SkillNormalizationService normalizationService;

    @Data
    @Builder
    public static class AnalysisResultSummary {
        private double overallScore;
        private double requiredCoverage;
        private double preferredCoverage;
        private double semanticScore;
        private double experienceAlignment;
        private double educationAlignment;
        private List<AnalysisSkillMatchEntity> matches;
        private List<AnalysisSkillGapEntity> gaps;
        private List<String> relatedEvidenceList;
        private String executiveSummary;
    }

    public AnalysisResultSummary performGapAnalysis(
            ResumeEntity resume,
            JobPostingEntity job,
            List<JobSkillEntity> jobSkills,
            List<ResumeSkillEntity> resumeSkills) {

        List<AnalysisSkillMatchEntity> matches = new ArrayList<>();
        List<AnalysisSkillGapEntity> gaps = new ArrayList<>();
        List<String> relatedEvidence = new ArrayList<>();

        int totalRequired = 0;
        int matchedRequired = 0;
        int totalPreferred = 0;
        int matchedPreferred = 0;
        double sumSimilarity = 0.0;

        for (JobSkillEntity jSkill : jobSkills) {
            boolean isReq = Boolean.TRUE.equals(jSkill.getIsRequired());
            if (isReq) totalRequired++;
            else totalPreferred++;

            SemanticMatchingService.MatchEvaluation eval = matchingService.evaluateSkill(jSkill, resumeSkills);
            sumSimilarity += eval.getSimilarityScore();

            String canonicalName = jSkill.getCanonicalSkill() != null ?
                    jSkill.getCanonicalSkill().getCanonicalName() :
                    normalizationService.normalizeSkill(jSkill.getRawName()).getCanonicalName();

            if (eval.isMatched()) {
                if (isReq) matchedRequired++;
                else matchedPreferred++;

                matches.add(AnalysisSkillMatchEntity.builder()
                        .jobSkillName(jSkill.getRawName())
                        .resumeSkillName(eval.getMatchedResumeSkill() != null ? eval.getMatchedResumeSkill().getRawName() : jSkill.getRawName())
                        .canonicalName(canonicalName)
                        .matchType(eval.getMatchType())
                        .similarityScore(Math.round(eval.getSimilarityScore() * 100.0) / 100.0)
                        .isRequired(isReq)
                        .explanation(eval.getExplanation())
                        .build());
            } else if (eval.isRelated()) {
                // Related partial match
                relatedEvidence.add(String.format("Requirement '%s': related experience found in '%s' (%.0f%% domain overlap)",
                        jSkill.getRawName(),
                        eval.getMatchedResumeSkill() != null ? eval.getMatchedResumeSkill().getRawName() : "adjacent technologies",
                        eval.getSimilarityScore() * 100));

                gaps.add(AnalysisSkillGapEntity.builder()
                        .jobSkillName(jSkill.getRawName())
                        .canonicalName(canonicalName)
                        .isRequired(isReq)
                        .gapSeverity(isReq ? "MODERATE" : "LOW")
                        .relatedEvidence(eval.getMatchedResumeSkill() != null ? eval.getMatchedResumeSkill().getRawName() : "Adjacent domain skills")
                        .explanation(eval.getExplanation())
                        .build());
            } else {
                // Definite skill gap
                String severity = isReq ? "CRITICAL" : "LOW";
                gaps.add(AnalysisSkillGapEntity.builder()
                        .jobSkillName(jSkill.getRawName())
                        .canonicalName(canonicalName)
                        .isRequired(isReq)
                        .gapSeverity(severity)
                        .relatedEvidence("No direct or related technology evidence identified in resume text.")
                        .explanation(String.format("Candidate does not explicitly mention '%s' or an equivalent canonical alias.", jSkill.getRawName()))
                        .build());
            }
        }

        double reqCoverage = totalRequired > 0 ? (double) matchedRequired / totalRequired : 1.0;
        double prefCoverage = totalPreferred > 0 ? (double) matchedPreferred / totalPreferred : 1.0;
        double avgSemantic = !jobSkills.isEmpty() ? sumSimilarity / jobSkills.size() : 0.0;

        // Experience alignment heuristic
        double expAlignment = evaluateExperienceAlignment(resume, job);
        double eduAlignment = evaluateEducationAlignment(resume, job);

        // Weighted composite score (avoiding naive hire probabilities)
        double overallScore = (reqCoverage * 0.50) + (prefCoverage * 0.15) + (avgSemantic * 0.20) + (expAlignment * 0.15);
        overallScore = Math.round(overallScore * 1000.0) / 10.0; // 0 to 100 scale

        String summary = generateExecutiveSummary(
                matchedRequired, totalRequired,
                matchedPreferred, totalPreferred,
                overallScore, gaps.size()
        );

        return AnalysisResultSummary.builder()
                .overallScore(overallScore)
                .requiredCoverage(Math.round(reqCoverage * 1000.0) / 10.0)
                .preferredCoverage(Math.round(prefCoverage * 1000.0) / 10.0)
                .semanticScore(Math.round(avgSemantic * 1000.0) / 10.0)
                .experienceAlignment(Math.round(expAlignment * 1000.0) / 10.0)
                .educationAlignment(Math.round(eduAlignment * 1000.0) / 10.0)
                .matches(matches)
                .gaps(gaps)
                .relatedEvidenceList(relatedEvidence)
                .executiveSummary(summary)
                .build();
    }

    private double evaluateExperienceAlignment(ResumeEntity resume, JobPostingEntity job) {
        String text = (resume.getRawText() != null ? resume.getRawText() : "").toLowerCase(Locale.ROOT);
        int reqYears = job.getRequiredExperienceYears() != null ? job.getRequiredExperienceYears() : 0;
        if (reqYears == 0) return 0.95; // Entry/Intern level

        if (text.contains("senior") || text.contains("lead") || text.contains("5+ years") || text.contains("4+ years")) {
            return 1.0;
        } else if (text.contains("engineer") || text.contains("developer") || text.contains("intern") || text.contains("project")) {
            return reqYears <= 2 ? 0.90 : 0.70;
        }
        return 0.60;
    }

    private double evaluateEducationAlignment(ResumeEntity resume, JobPostingEntity job) {
        String text = (resume.getRawText() != null ? resume.getRawText() : "").toLowerCase(Locale.ROOT);
        String reqDeg = (job.getRequiredDegree() != null ? job.getRequiredDegree() : "").toLowerCase(Locale.ROOT);

        if (reqDeg.isBlank() || reqDeg.contains("any") || reqDeg.contains("bachelor")) {
            if (text.contains("bachelor") || text.contains("b.s.") || text.contains("computer science") || text.contains("university") || text.contains("college")) {
                return 1.0;
            }
            return 0.85;
        }
        if (reqDeg.contains("master") && text.contains("master")) {
            return 1.0;
        }
        return 0.80;
    }

    private String generateExecutiveSummary(int matchedReq, int totalReq, int matchedPref, int totalPref, double overallScore, int totalGaps) {
        return String.format(
                "Resume demonstrates a %.1f%% composite compatibility with this role. " +
                "Successfully matched %d of %d required technical competencies and %d of %d preferred competencies. " +
                "Identified %d potential skill gap%s requiring attention or resume keyword alignment.",
                overallScore,
                matchedReq, totalReq,
                matchedPref, totalPref,
                totalGaps, totalGaps == 1 ? "" : "s"
        );
    }
}
