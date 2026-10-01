package com.resume.resume_api.ingestion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.resume_api.normalization.SkillNormalizationService;
import com.resume.resume_api.normalization.TextNormalizer;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobDescriptionParserService {

    private final SkillNormalizationService normalizationService;
    private final ObjectMapper objectMapper;

    @Data
    @Builder
    public static class ParsedJobProfile {
        private String title;
        private String company;
        private String location;
        private String employmentType;
        private String summary;
        private int requiredExperienceYears;
        private String requiredDegree;
        private List<ParsedJobSkill> extractedSkills;
        private String parsedJson;
    }

    @Data
    @Builder
    public static class ParsedJobSkill {
        private String rawName;
        private String canonicalName;
        private String normalizedName;
        private String category;
        private boolean isRequired;
        private String importance; // CRITICAL, HIGH, MEDIUM, PREFERRED
    }

    public ParsedJobProfile parse(String title, String company, String rawText) {
        String clean = TextNormalizer.cleanText(rawText);
        String detectedTitle = (title != null && !title.isBlank()) ? title.trim() : extractTitle(clean);
        String detectedCompany = (company != null && !company.isBlank()) ? company.trim() : extractCompany(clean);
        String location = extractLocation(clean);
        String employmentType = extractEmploymentType(clean);
        int expYears = extractExperienceYears(clean);
        String degree = extractDegree(clean);

        List<ParsedJobSkill> skills = extractSkills(clean);

        String json = "{}";
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("title", detectedTitle);
            map.put("company", detectedCompany);
            map.put("location", location);
            map.put("employmentType", employmentType);
            map.put("experienceYears", expYears);
            map.put("degree", degree);
            map.put("totalSkillsFound", skills.size());
            map.put("skills", skills);
            json = objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.warn("Failed to serialize job profile to JSON", e);
        }

        String summary = clean.length() > 250 ? clean.substring(0, 250) + "..." : clean;

        return ParsedJobProfile.builder()
                .title(detectedTitle)
                .company(detectedCompany)
                .location(location)
                .employmentType(employmentType)
                .summary(summary)
                .requiredExperienceYears(expYears)
                .requiredDegree(degree)
                .extractedSkills(skills)
                .parsedJson(json)
                .build();
    }

    private String extractTitle(String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() >= 4 && trimmed.length() <= 60 &&
                    (trimmed.toLowerCase().contains("engineer") ||
                     trimmed.toLowerCase().contains("developer") ||
                     trimmed.toLowerCase().contains("architect") ||
                     trimmed.toLowerCase().contains("intern") ||
                     trimmed.toLowerCase().contains("analyst"))) {
                return trimmed;
            }
        }
        return "Software Engineer";
    }

    private String extractCompany(String text) {
        Pattern compPat = Pattern.compile("(?i)(?:at|company:?|about)\\s+([A-Z][a-zA-Z0-9&.,\\s]{2,25})");
        Matcher m = compPat.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }
        return "Hiring Organization";
    }

    private String extractLocation(String text) {
        Pattern locPat = Pattern.compile("(?i)(Remote|Hybrid|On-site|Dallas|Austin|San Francisco|Seattle|New York|Chicago|TX|CA|NY)");
        Matcher m = locPat.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return "Flexible / Remote";
    }

    private String extractEmploymentType(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("internship") || lower.contains("intern")) return "Internship";
        if (lower.contains("contract")) return "Contract";
        if (lower.contains("part-time")) return "Part-time";
        return "Full-time";
    }

    private int extractExperienceYears(String text) {
        Pattern expPat = Pattern.compile("(?i)(\\d+)\\+?\\s*(?:to\\s*\\d+)?\\s*years?(?:\\s*of)?\\s*(?:relevant\\s*)?experience");
        Matcher m = expPat.matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private String extractDegree(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("master") || lower.contains("m.s.")) return "Master's in Computer Science or related field";
        if (lower.contains("bachelor") || lower.contains("b.s.") || lower.contains("degree in computer science")) {
            return "Bachelor's in Computer Science or related field";
        }
        return "Bachelor's or equivalent practical experience";
    }

    private List<ParsedJobSkill> extractSkills(String text) {
        Map<String, ParsedJobSkill> map = new LinkedHashMap<>();

        // Partition text into Preferred Section vs Required Section
        String lower = text.toLowerCase(Locale.ROOT);
        int preferredIdx = Math.max(lower.indexOf("preferred"), Math.max(lower.indexOf("nice to have"), lower.indexOf("bonus")));

        String[] skillsCatalog = {
                "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "Go", "Rust", "SQL", "HTML/CSS",
                "Spring Boot", "Spring", "Spring AI", "React", "Next.js", "Node.js", "Express.js", "Angular", "Vue.js", "Django", "FastAPI",
                "PostgreSQL", "Postgres", "PGVector", "MySQL", "MongoDB", "Redis", "Vector Databases", "Elasticsearch",
                "AWS", "Amazon Web Services", "Azure", "GCP", "Google Cloud", "Docker", "Kubernetes", "CI/CD", "Terraform", "Linux", "Git",
                "RAG", "Retrieval-Augmented Generation", "Machine Learning", "Artificial Intelligence", "NLP", "Embeddings", "LLM",
                "REST API", "Microservices", "GraphQL", "Kafka", "System Design", "OOP", "Unit Testing", "JUnit", "Mockito", "Bootstrap", "Tailwind CSS"
        };

        for (String skillCandidate : skillsCatalog) {
            Pattern p = Pattern.compile("(?i)\\b" + Pattern.quote(skillCandidate) + "\\b");
            Matcher m = p.matcher(text);
            if (m.find()) {
                SkillNormalizationService.NormalizedSkillResult norm = normalizationService.normalizeSkill(skillCandidate);
                boolean isPreferred = (preferredIdx != -1 && m.start() >= preferredIdx);
                boolean isRequired = !isPreferred;
                String importance = isRequired ? "HIGH" : "PREFERRED";

                map.put(norm.getNormalizedName(), ParsedJobSkill.builder()
                        .rawName(skillCandidate)
                        .canonicalName(norm.getCanonicalName())
                        .normalizedName(norm.getNormalizedName())
                        .category(norm.getCategory())
                        .isRequired(isRequired)
                        .importance(importance)
                        .build());
            }
        }

        return new ArrayList<>(map.values());
    }
}
