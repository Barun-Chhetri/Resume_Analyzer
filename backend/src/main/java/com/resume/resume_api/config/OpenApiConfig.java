package com.resume.resume_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Resume Intelligence Platform API")
                        .version("1.0.0")
                        .description("Production-grade AI Resume Intelligence Platform built with Java 21, Spring Boot, Spring AI, PostgreSQL, PGVector, and RAG architecture. Ingests resumes (PDF, DOCX, TXT), normalizes technical skills to canonical taxonomies, generates 1536-dimensional dense vector embeddings, performs semantic similarity matching against job requirements, identifies skill gaps, and synthesizes grounded recommendations.")
                        .contact(new Contact()
                                .name("Barun Chhetri")
                                .email("barun.chhetri@example.com")
                                .url("https://github.com/barunchhetri"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
