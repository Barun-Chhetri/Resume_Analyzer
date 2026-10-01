package com.resume.resume_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.resume_api.ai.EmbeddingService;
import com.resume.resume_api.ai.LocalDenseEmbeddingModel;
import com.resume.resume_api.entity.JobPostingEntity;
import com.resume.resume_api.entity.JobSkillEntity;
import com.resume.resume_api.entity.ResumeEntity;
import com.resume.resume_api.entity.ResumeSkillEntity;
import com.resume.resume_api.matching.SemanticMatchingService;
import com.resume.resume_api.matching.SkillGapAnalysisService;
import com.resume.resume_api.normalization.SkillNormalizationService;
import com.resume.resume_api.repository.CanonicalSkillRepository;
import com.resume.resume_api.repository.SkillAliasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkillGapAnalysisServiceTest {

    private SkillGapAnalysisService gapAnalysisService;

    @BeforeEach
    void setUp() {
        CanonicalSkillRepository canonicalRepo = Mockito.mock(CanonicalSkillRepository.class);
        SkillAliasRepository aliasRepo = Mockito.mock(SkillAliasRepository.class);
        SkillNormalizationService normService = new SkillNormalizationService(canonicalRepo, aliasRepo);
        normService.init();

        EmbeddingService embService = new EmbeddingService(new LocalDenseEmbeddingModel(), new ObjectMapper());
        ReflectionTestUtils.setField(embService, "providerConfig", "local");
        ReflectionTestUtils.setField(embService, "openAiApiKey", "demo-key");

        SemanticMatchingService matchingService = new SemanticMatchingService(normService, embService);
        ReflectionTestUtils.setField(matchingService, "semanticMatchThreshold", 0.65);
        ReflectionTestUtils.setField(matchingService, "relatedSkillThreshold", 0.50);

        gapAnalysisService = new SkillGapAnalysisService(matchingService, normService);
    }

    @Test
    @DisplayName("Should accurately partition matched skills from gaps and compute coverage percentage")
    void testGapAnalysisPartitioning() {
        ResumeEntity resume = ResumeEntity.builder()
                .candidateName("Barun Chhetri")
                .rawText("Proficient in Java, Spring Boot, PostgreSQL, and Docker.")
                .build();

        JobPostingEntity job = JobPostingEntity.builder()
                .title("Software Engineer")
                .requiredExperienceYears(1)
                .build();

        List<ResumeSkillEntity> resumeSkills = List.of(
                ResumeSkillEntity.builder().rawName("Java").build(),
                ResumeSkillEntity.builder().rawName("Spring Boot").build(),
                ResumeSkillEntity.builder().rawName("PostgreSQL").build(),
                ResumeSkillEntity.builder().rawName("Docker").build()
        );

        List<JobSkillEntity> jobSkills = List.of(
                JobSkillEntity.builder().rawName("Java").isRequired(true).build(),
                JobSkillEntity.builder().rawName("Spring Boot").isRequired(true).build(),
                JobSkillEntity.builder().rawName("PostgreSQL").isRequired(true).build(),
                JobSkillEntity.builder().rawName("AWS").isRequired(true).build(),
                JobSkillEntity.builder().rawName("Figma").isRequired(false).build() // Preferred unrelated skill
        );

        SkillGapAnalysisService.AnalysisResultSummary result =
                gapAnalysisService.performGapAnalysis(resume, job, jobSkills, resumeSkills);

        assertThat(result.getMatches()).hasSize(3); // Java, Spring Boot, PostgreSQL
        assertThat(result.getGaps()).hasSize(2);    // AWS, Figma

        // 3 out of 4 required matched = 75%
        assertThat(result.getRequiredCoverage()).isEqualTo(75.0);

        // Gap severity check: AWS (required) -> CRITICAL
        boolean hasCriticalAws = result.getGaps().stream()
                .anyMatch(g -> g.getJobSkillName().equals("AWS") && "CRITICAL".equals(g.getGapSeverity()));
        assertThat(hasCriticalAws).isTrue();
    }
}
