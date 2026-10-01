package com.resume.resume_api;

import com.resume.resume_api.normalization.SkillNormalizationService;
import com.resume.resume_api.normalization.TextNormalizer;
import com.resume.resume_api.repository.CanonicalSkillRepository;
import com.resume.resume_api.repository.SkillAliasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class SkillNormalizationServiceTest {

    private SkillNormalizationService service;
    private CanonicalSkillRepository canonicalRepo;
    private SkillAliasRepository aliasRepo;

    @BeforeEach
    void setUp() {
        canonicalRepo = Mockito.mock(CanonicalSkillRepository.class);
        aliasRepo = Mockito.mock(SkillAliasRepository.class);
        service = new SkillNormalizationService(canonicalRepo, aliasRepo);
        service.init();
    }

    @Test
    @DisplayName("Should normalize common language variations to canonical entities")
    void testLanguageNormalization() {
        assertThat(service.normalizeSkill("JavaScript").getCanonicalName()).isEqualTo("JavaScript");
        assertThat(service.normalizeSkill("JS").getCanonicalName()).isEqualTo("JavaScript");
        assertThat(service.normalizeSkill("es6").getCanonicalName()).isEqualTo("JavaScript");
        assertThat(service.normalizeSkill("TypeScript").getCanonicalName()).isEqualTo("TypeScript");
        assertThat(service.normalizeSkill("TS").getCanonicalName()).isEqualTo("TypeScript");
    }

    @Test
    @DisplayName("Should normalize database variants to canonical PostgreSQL and PGVector")
    void testDatabaseNormalization() {
        assertThat(service.normalizeSkill("PostgreSQL").getCanonicalName()).isEqualTo("PostgreSQL");
        assertThat(service.normalizeSkill("postgres").getCanonicalName()).isEqualTo("PostgreSQL");
        assertThat(service.normalizeSkill("Postgres SQL").getCanonicalName()).isEqualTo("PostgreSQL");
        assertThat(service.normalizeSkill("PGVector").getCanonicalName()).isEqualTo("PGVector");
        assertThat(service.normalizeSkill("pg vector").getCanonicalName()).isEqualTo("PGVector");
    }

    @Test
    @DisplayName("Should normalize cloud and devops aliases like AWS and K8s")
    void testCloudAndDevOpsNormalization() {
        assertThat(service.normalizeSkill("AWS").getCanonicalName()).isEqualTo("Amazon Web Services");
        assertThat(service.normalizeSkill("Amazon Web Services").getCanonicalName()).isEqualTo("Amazon Web Services");
        assertThat(service.normalizeSkill("k8s").getCanonicalName()).isEqualTo("Kubernetes");
        assertThat(service.normalizeSkill("docker").getCanonicalName()).isEqualTo("Docker");
        assertThat(service.normalizeSkill("ci/cd").getCanonicalName()).isEqualTo("CI/CD");
    }

    @Test
    @DisplayName("Should normalize AI and RAG technologies correctly")
    void testAiAndRagNormalization() {
        assertThat(service.normalizeSkill("RAG").getCanonicalName()).isEqualTo("Retrieval-Augmented Generation");
        assertThat(service.normalizeSkill("ML").getCanonicalName()).isEqualTo("Machine Learning");
        assertThat(service.normalizeSkill("AI").getCanonicalName()).isEqualTo("Artificial Intelligence");
        assertThat(service.normalizeSkill("Spring AI").getCanonicalName()).isEqualTo("Spring AI");
    }

    @Test
    @DisplayName("TextNormalizer should clean symbols without destroying semantic key")
    void testTextNormalizer() {
        assertThat(TextNormalizer.normalize("C++")).isEqualTo("cpp");
        assertThat(TextNormalizer.normalize("C#")).isEqualTo("csharp");
        assertThat(TextNormalizer.normalize("Node.js")).isEqualTo("nodejs");
        assertThat(TextNormalizer.normalize("Next.js")).isEqualTo("nextjs");
    }
}
