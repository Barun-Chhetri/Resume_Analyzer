package com.resume.resume_api.ai;

import com.resume.resume_api.entity.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRecommendationEngine {

    private final RagRetrievalService ragRetrievalService;

    @Data
    @Builder
    public static class RecommendationItem {
        private String category; // SKILL_TO_LEARN, RESUME_EMPHASIS, PROJECT_SUGGESTION, BULLET_IMPROVEMENT, INTERVIEW_PREPARATION
        private String title;
        private String description;
        private String priority; // HIGH, MEDIUM, LOW
        private String actionableSteps;
    }

    /**
     * Generates grounded, highly actionable recommendations using retrieved context
     * from resume sections and skill gaps. Strictly avoids hallucination.
     */
    public List<RecommendationItem> generateRecommendations(
            ResumeEntity resume,
            JobPostingEntity job,
            List<AnalysisSkillMatchEntity> matches,
            List<AnalysisSkillGapEntity> gaps) {

        List<RecommendationItem> recs = new ArrayList<>();
        String rawResumeText = (resume.getRawText() != null ? resume.getRawText() : "").toLowerCase(Locale.ROOT);

        // 1. Recommendations for critical required skill gaps
        List<AnalysisSkillGapEntity> criticalGaps = gaps.stream()
                .filter(g -> Boolean.TRUE.equals(g.getIsRequired()))
                .limit(3)
                .toList();

        for (AnalysisSkillGapEntity gap : criticalGaps) {
            String skill = gap.getJobSkillName();
            // Check if related evidence exists on the resume
            List<RagRetrievalService.RetrievedDocument> relatedDocs = ragRetrievalService.retrieveRelevantExperience(resume.getId(), skill);

            String evidenceNote = "";
            if (!relatedDocs.isEmpty() && relatedDocs.get(0).getSimilarityScore() > 0.45) {
                evidenceNote = String.format(" Your resume mentions related experience in: \"%s\", but '%s' is not explicitly listed.",
                        truncate(relatedDocs.get(0).getContent(), 120), skill);
            }

            recs.add(RecommendationItem.builder()
                    .category("SKILL_TO_LEARN")
                    .title("Bridge Required Competency: " + skill)
                    .description(String.format("The job posting specifically requires '%s'.%s Explicitly listing this skill or completing a practical project will significantly increase qualification match.", skill, evidenceNote))
                    .priority("HIGH")
                    .actionableSteps(String.format("1. Build a mini-project integrating %s with your existing Java/Spring Boot stack.\n2. Document design decisions, setup instructions, and architecture in a GitHub repository.\n3. Add %s under Technical Skills on your resume once practical foundation is established.", skill, skill))
                    .build());
        }

        // 2. Resume bullet improvements (quantifiable impact & action verbs)
        boolean hasMetrics = rawResumeText.contains("%") || rawResumeText.contains("ms") || rawResumeText.contains("reduced") || rawResumeText.contains("improved");
        if (!hasMetrics) {
            recs.add(RecommendationItem.builder()
                    .category("BULLET_IMPROVEMENT")
                    .title("Inject Quantifiable Impact & Metrics into Experience")
                    .description("Your resume descriptions outline responsibilities but lack measurable business or engineering impact (e.g. latency reductions, query optimization percentages, test coverage).")
                    .priority("HIGH")
                    .actionableSteps("Format bullet points using the Google XYZ Formula: 'Accomplished [X], as measured by [Y], by doing [Z]'. Example: 'Reduced API response times by 35% by implementing Redis caching and indexing PostgreSQL queries.'")
                    .build());
        } else {
            recs.add(RecommendationItem.builder()
                    .category("BULLET_IMPROVEMENT")
                    .title("Align Project Impact to Target Role Responsibilities")
                    .description(String.format("Tailor your existing project bullets to explicitly emphasize backend systems and architecture relevant to %s at %s.",
                            job.getTitle(), job.getCompany() != null ? job.getCompany() : "the target employer"))
                    .priority("MEDIUM")
                    .actionableSteps("Ensure each project bullet starts with a strong action verb (Architected, Implemented, Engineered, Deployed) and directly highlights the technologies required by the job posting.")
                    .build());
        }

        // 3. Recommended portfolio project addressing skill gaps
        if (!criticalGaps.isEmpty()) {
            String primaryGap = criticalGaps.get(0).getJobSkillName();
            recs.add(RecommendationItem.builder()
                    .category("PROJECT_SUGGESTION")
                    .title("Build a Portfolio Project Demonstrating " + primaryGap)
                    .description(String.format("To prove practical capability without prior formal job experience in %s, construct an end-to-end demonstrable application combining your core strengths with %s.", primaryGap, primaryGap))
                    .priority("MEDIUM")
                    .actionableSteps(String.format("Create a full-stack or backend service utilizing Java, Spring Boot, PostgreSQL, and %s. Include Docker containerization, comprehensive JUnit tests, and a clear architectural diagram in your README.", primaryGap))
                    .build());
        }

        // 4. Skills already present that should be emphasized more prominently
        List<AnalysisSkillMatchEntity> matchesToEmphasize = matches.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsRequired()))
                .limit(2)
                .toList();

        if (!matchesToEmphasize.isEmpty()) {
            StringBuilder skillNames = new StringBuilder();
            for (int i = 0; i < matchesToEmphasize.size(); i++) {
                if (i > 0) skillNames.append(", ");
                skillNames.append(matchesToEmphasize.get(i).getJobSkillName());
            }

            recs.add(RecommendationItem.builder()
                    .category("RESUME_EMPHASIS")
                    .title("Elevate Core Matched Competencies: " + skillNames)
                    .description(String.format("You already possess verified skills in %s which are crucial for this job. Ensure these technologies appear in your resume summary, technical skills header, and in at least two project bullet points.", skillNames))
                    .priority("MEDIUM")
                    .actionableSteps("Move these key matching technologies to the first 3 items in your Technical Skills section so recruiters immediately recognize role alignment within the first 6 seconds of review.")
                    .build());
        }

        // 5. Technical Interview Preparation
        recs.add(RecommendationItem.builder()
                .category("INTERVIEW_PREPARATION")
                .title("Prepare System Design & Architectural Trade-offs")
                .description(String.format("For a role in %s requiring %s, expect in-depth questions on concurrency, database indexing, REST API design, and distributed systems architecture.",
                        job.getTitle(), !matches.isEmpty() ? matches.get(0).getJobSkillName() : "backend development"))
                .priority("LOW")
                .actionableSteps("Review database query execution plans (EXPLAIN ANALYZE), connection pooling best practices (HikariCP), ACID properties in PostgreSQL, and semantic search/vector retrieval workflows.")
                .build());

        return recs;
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen) + "...";
    }
}
