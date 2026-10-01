package com.resume.resume_api.config;

import com.resume.resume_api.entity.*;
import com.resume.resume_api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({"dev", "test"})
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer implements CommandLineRunner {

    private final ContactRepository contactRepo;
    private final SkillRepository skillRepo;
    private final EducationRepository educationRepo;
    private final ExperienceRepository experienceRepo;
    private final CanonicalSkillRepository canonicalSkillRepo;
    private final SkillAliasRepository skillAliasRepo;

    @Override
    public void run(String... args) {
        log.info("Running DevDataInitializer to seed baseline entities...");

        if (contactRepo.count() == 0) {
            Contact contact = new Contact(
                    null,
                    "Barun Chhetri",
                    "Fullstack Developer & AI Engineer",
                    "+1 (555) 019-2834",
                    "barun.chhetri@example.com",
                    "https://github.com/barunchhetri",
                    "Dallas-Fort Worth, TX",
                    "Passionate Software Engineer skilled in Java, Spring Boot, Spring AI, PostgreSQL, and scalable web architectures."
            );
            contactRepo.save(contact);
        }

        if (skillRepo.count() == 0) {
            skillRepo.saveAll(List.of(
                    new Skill(null, "Java"),
                    new Skill(null, "Spring Boot"),
                    new Skill(null, "PostgreSQL"),
                    new Skill(null, "Docker"),
                    new Skill(null, "React"),
                    new Skill(null, "REST APIs"),
                    new Skill(null, "Git")
            ));
        }

        if (educationRepo.count() == 0) {
            educationRepo.save(new Education(null, "B.S. in Computer Science", "University of North Texas", "2023 - 2026"));
        }

        if (experienceRepo.count() == 0) {
            Experience exp = new Experience(
                    null,
                    "Software Engineering Intern",
                    "Tech Innovation Labs",
                    "Summer 2025",
                    List.of(
                            "Architected high-throughput REST APIs using Spring Boot and PostgreSQL.",
                            "Optimized database indexing and HikariCP connection pooling, improving p95 query latency by 42%.",
                            "Containerized backend microservices with Docker and automated deployment with GitHub Actions."
                    )
            );
            experienceRepo.save(exp);
        }

        // Seed Canonical Skills in dev mode
        if (canonicalSkillRepo.count() == 0) {
            seedCanonicalSkill("Java", "java", "Languages", "Object-oriented programming language for enterprise backends.",
                    List.of("java", "core java", "java 21", "java 17", "java 8"));
            seedCanonicalSkill("Python", "python", "Languages", "High-level language dominant in AI, ML, and automation.",
                    List.of("python", "python 3", "py"));
            seedCanonicalSkill("Spring Boot", "springboot", "Frameworks", "Enterprise Java framework for stand-alone microservices.",
                    List.of("spring", "spring boot", "springboot", "spring framework", "spring data"));
            seedCanonicalSkill("PostgreSQL", "postgresql", "Databases", "Advanced open-source relational database.",
                    List.of("postgresql", "postgres", "postgres sql", "psql"));
            seedCanonicalSkill("PGVector", "pgvector", "Databases", "Vector similarity search extension for PostgreSQL.",
                    List.of("pgvector", "pg vector", "postgres vector"));
            seedCanonicalSkill("Docker", "docker", "Cloud & DevOps", "Containerization platform for reliable application deployment.",
                    List.of("docker", "docker compose", "containerization"));
            seedCanonicalSkill("Kubernetes", "kubernetes", "Cloud & DevOps", "Container orchestration system.",
                    List.of("kubernetes", "k8s"));
            seedCanonicalSkill("Amazon Web Services", "aws", "Cloud & DevOps", "Cloud platform provided by Amazon.",
                    List.of("aws", "amazon web services", "amazon aws", "ec2", "s3"));
            seedCanonicalSkill("React", "react", "Frameworks", "Declarative JavaScript library for UI development.",
                    List.of("react", "reactjs", "react.js"));
            seedCanonicalSkill("REST API", "restapi", "Architecture", "Representational State Transfer web architecture.",
                    List.of("rest", "rest api", "restful", "restful api"));
            seedCanonicalSkill("Retrieval-Augmented Generation", "rag", "AI/ML", "Vector retrieval architecture grounding LLMs.",
                    List.of("rag", "retrieval augmented generation", "retrieval-augmented generation"));
            seedCanonicalSkill("Machine Learning", "machinelearning", "AI/ML", "Algorithms that learn patterns from data.",
                    List.of("machine learning", "ml"));
        }

        log.info("DevDataInitializer completed. Canonical skills count: {}", canonicalSkillRepo.count());
    }

    private void seedCanonicalSkill(String name, String norm, String category, String desc, List<String> aliases) {
        CanonicalSkillEntity skill = CanonicalSkillEntity.builder()
                .canonicalName(name)
                .normalizedName(norm)
                .category(category)
                .description(desc)
                .build();
        CanonicalSkillEntity saved = canonicalSkillRepo.save(skill);

        for (String a : aliases) {
            String normAlias = a.toLowerCase().replaceAll("[^a-z0-9]", "");
            SkillAliasEntity alias = SkillAliasEntity.builder()
                    .canonicalSkill(saved)
                    .alias(a)
                    .normalizedAlias(normAlias)
                    .build();
            skillAliasRepo.save(alias);
        }
    }
}
