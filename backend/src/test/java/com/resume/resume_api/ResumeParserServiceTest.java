package com.resume.resume_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.resume_api.ingestion.ResumeParserService;
import com.resume.resume_api.normalization.SkillNormalizationService;
import com.resume.resume_api.repository.CanonicalSkillRepository;
import com.resume.resume_api.repository.SkillAliasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeParserServiceTest {

    private ResumeParserService parser;

    @BeforeEach
    void setUp() {
        CanonicalSkillRepository canonicalRepo = Mockito.mock(CanonicalSkillRepository.class);
        SkillAliasRepository aliasRepo = Mockito.mock(SkillAliasRepository.class);
        SkillNormalizationService normService = new SkillNormalizationService(canonicalRepo, aliasRepo);
        normService.init();

        parser = new ResumeParserService(normService, new ObjectMapper());
    }

    @Test
    @DisplayName("Should extract candidate contact info, sections, and technical skills from resume text")
    void testResumeParsing() {
        String resumeText = """
                BARUN CHHETRI
                Email: barun.chhetri@example.com | Phone: (555) 123-4567 | Dallas, TX
                
                SUMMARY
                Results-driven Software Engineer with extensive experience building scalable REST APIs and microservices.
                
                TECHNICAL SKILLS
                Languages: Java, Python, TypeScript, SQL
                Frameworks & Libraries: Spring Boot, React, Next.js, Spring AI
                Databases & Cloud: PostgreSQL, PGVector, Docker, AWS
                
                EXPERIENCE
                Software Engineer Intern - Tech Labs
                June 2025 - August 2025
                - Architected high-performance REST APIs using Spring Boot and PostgreSQL.
                - Containerized services with Docker and deployed automated CI/CD pipelines.
                
                EDUCATION
                University of North Texas
                B.S. in Computer Science, 2026
                """;

        ResumeParserService.ParsedResumeProfile profile = parser.parse(resumeText);

        assertThat(profile.getCandidateName()).contains("BARUN CHHETRI");
        assertThat(profile.getEmail()).isEqualTo("barun.chhetri@example.com");
        assertThat(profile.getPhone()).isEqualTo("(555) 123-4567");
        assertThat(profile.getSections()).containsKey("SUMMARY");
        assertThat(profile.getSections()).containsKey("SKILLS");
        assertThat(profile.getSections()).containsKey("EXPERIENCE");
        assertThat(profile.getSections()).containsKey("EDUCATION");

        assertThat(profile.getExtractedSkills()).isNotEmpty();
        boolean hasJava = profile.getExtractedSkills().stream().anyMatch(s -> s.getCanonicalName().equals("Java"));
        boolean hasSpringBoot = profile.getExtractedSkills().stream().anyMatch(s -> s.getCanonicalName().equals("Spring Boot"));
        boolean hasPostgres = profile.getExtractedSkills().stream().anyMatch(s -> s.getCanonicalName().equals("PostgreSQL"));

        assertThat(hasJava).isTrue();
        assertThat(hasSpringBoot).isTrue();
        assertThat(hasPostgres).isTrue();
    }
}
