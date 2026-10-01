package com.resume.resume_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.resume_api.ai.EmbeddingService;
import com.resume.resume_api.ai.LocalDenseEmbeddingModel;
import com.resume.resume_api.entity.JobSkillEntity;
import com.resume.resume_api.entity.ResumeSkillEntity;
import com.resume.resume_api.matching.SemanticMatchingService;
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

class SemanticMatchingServiceTest {

    private SemanticMatchingService matchingService;

    @BeforeEach
    void setUp() {
        CanonicalSkillRepository canonicalRepo = Mockito.mock(CanonicalSkillRepository.class);
        SkillAliasRepository aliasRepo = Mockito.mock(SkillAliasRepository.class);
        SkillNormalizationService normService = new SkillNormalizationService(canonicalRepo, aliasRepo);
        normService.init();

        EmbeddingService embService = new EmbeddingService(new LocalDenseEmbeddingModel(), new ObjectMapper());
        ReflectionTestUtils.setField(embService, "providerConfig", "local");
        ReflectionTestUtils.setField(embService, "openAiApiKey", "demo-key");

        matchingService = new SemanticMatchingService(normService, embService);
        ReflectionTestUtils.setField(matchingService, "semanticMatchThreshold", 0.65);
        ReflectionTestUtils.setField(matchingService, "relatedSkillThreshold", 0.50);
    }

    @Test
    @DisplayName("Should detect EXACT match when skill string matches exactly")
    void testExactMatch() {
        JobSkillEntity jobSkill = JobSkillEntity.builder().rawName("Java").isRequired(true).build();
        ResumeSkillEntity resumeSkill = ResumeSkillEntity.builder().rawName("Java").build();

        SemanticMatchingService.MatchEvaluation eval = matchingService.evaluateSkill(jobSkill, List.of(resumeSkill));

        assertThat(eval.isMatched()).isTrue();
        assertThat(eval.getMatchType()).isEqualTo("EXACT");
        assertThat(eval.getSimilarityScore()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should detect ALIAS match when resume has 'Amazon Web Services' and job has 'AWS'")
    void testAliasMatch() {
        JobSkillEntity jobSkill = JobSkillEntity.builder().rawName("AWS").isRequired(true).build();
        ResumeSkillEntity resumeSkill = ResumeSkillEntity.builder().rawName("Amazon Web Services").build();

        SemanticMatchingService.MatchEvaluation eval = matchingService.evaluateSkill(jobSkill, List.of(resumeSkill));

        assertThat(eval.isMatched()).isTrue();
        assertThat(eval.getMatchType()).isEqualTo("ALIAS");
    }

    @Test
    @DisplayName("Should detect gap when skill is absent from resume")
    void testMissingSkillGap() {
        JobSkillEntity jobSkill = JobSkillEntity.builder().rawName("Kubernetes").isRequired(true).build();
        ResumeSkillEntity resumeSkill = ResumeSkillEntity.builder().rawName("Figma").build();

        SemanticMatchingService.MatchEvaluation eval = matchingService.evaluateSkill(jobSkill, List.of(resumeSkill));

        assertThat(eval.isMatched()).isFalse();
        assertThat(eval.getMatchType()).isEqualTo("NONE");
    }
}
