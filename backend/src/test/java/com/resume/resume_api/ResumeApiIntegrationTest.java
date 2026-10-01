package com.resume.resume_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResumeApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/resume (legacy endpoint) should return 200 OK and ResumeDTO structure")
    void testLegacyResumeEndpoint() throws Exception {
        mockMvc.perform(get("/api/resume"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/architecture should return system architecture metadata and vector specs")
    void testArchitectureEndpoint() throws Exception {
        mockMvc.perform(get("/api/architecture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vectorDimensions").value(1536))
                .andExpect(jsonPath("$.ragPipelineStages").isArray());
    }

    @Test
    @DisplayName("End-to-End Workflow: Ingest Resume -> Create Job -> Run Analysis -> Verify Gaps & Recommendations")
    void testEndToEndWorkflow() throws Exception {
        // 1. Ingest Resume
        String resumePayload = objectMapper.writeValueAsString(Map.of(
                "candidateName", "Barun Chhetri",
                "filename", "barun_resume.txt",
                "rawText", """
                        BARUN CHHETRI
                        Software Engineer | barun.chhetri@example.com | Dallas, TX
                        
                        SUMMARY
                        Passionate backend software engineer building cloud-native applications with Java, Spring Boot, and PostgreSQL.
                        
                        SKILLS
                        Languages: Java, SQL, Python
                        Frameworks: Spring Boot, Spring AI, React
                        Tools: PostgreSQL, Docker, Git, REST APIs
                        
                        EXPERIENCE
                        Software Engineer Intern
                        - Engineered RESTful APIs using Spring Boot and PostgreSQL.
                        - Containerized microservices using Docker.
                        """
        ));

        MvcResult resumeResult = mockMvc.perform(post("/api/resumes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resumePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        String resumeId = objectMapper.readTree(resumeResult.getResponse().getContentAsString()).get("id").asText();

        // 2. Create Job Posting
        String jobPayload = objectMapper.writeValueAsString(Map.of(
                "title", "Senior Java Backend Engineer",
                "company", "Enterprise Cloud Systems",
                "location", "Remote",
                "rawText", """
                        Senior Java Backend Engineer
                        Enterprise Cloud Systems
                        
                        Qualifications & Required Skills:
                        - 3+ years of experience with Java and Spring Boot microservices
                        - Strong proficiency in relational databases, especially PostgreSQL
                        - Hands-on experience with Docker and containerization
                        - Experience with Amazon Web Services (AWS)
                        - Knowledge of Kubernetes for cloud orchestration
                        
                        Preferred Skills:
                        - Familiarity with AI, RAG, and Vector Databases (PGVector)
                        """
        ));

        MvcResult jobResult = mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        String jobId = objectMapper.readTree(jobResult.getResponse().getContentAsString()).get("id").asText();

        // 3. Execute Analysis
        String analysisPayload = objectMapper.writeValueAsString(Map.of(
                "resumeId", resumeId,
                "jobId", jobId
        ));

        MvcResult analysisResult = mockMvc.perform(post("/api/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(analysisPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.overallMatchScore").isNumber())
                .andExpect(jsonPath("$.matches").isArray())
                .andExpect(jsonPath("$.gaps").isArray())
                .andExpect(jsonPath("$.recommendations").isArray())
                .andReturn();

        String analysisJson = analysisResult.getResponse().getContentAsString();
        assertThat(analysisJson).contains("Java");
        assertThat(analysisJson).contains("Spring Boot");
        assertThat(analysisJson).contains("PostgreSQL");

        // Verify that AWS or Kubernetes was flagged as a gap
        assertThat(analysisJson).contains("Kubernetes");
    }
}
