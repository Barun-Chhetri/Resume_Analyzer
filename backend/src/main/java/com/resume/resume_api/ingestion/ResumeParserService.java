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
public class ResumeParserService {

    private final SkillNormalizationService normalizationService;
    private final ObjectMapper objectMapper;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?1[-. ]?)?\\(?([0-9]{3})\\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})");

    @Data
    @Builder
    public static class ParsedResumeProfile {
        private String candidateName;
        private String email;
        private String phone;
        private String location;
        private String summary;
        private Map<String, String> sections; // SUMMARY, EXPERIENCE, EDUCATION, SKILLS, PROJECTS, CERTIFICATIONS
        private List<ParsedSkill> extractedSkills;
        private String parsedJson;
    }

    @Data
    @Builder
    public static class ParsedSkill {
        private String rawName;
        private String canonicalName;
        private String normalizedName;
        private String category;
        private String contextSnippet;
        private double confidence;
    }

    public ParsedResumeProfile parse(String rawText) {
        String clean = TextNormalizer.cleanText(rawText);
        String candidateName = extractCandidateName(clean);
        String email = extractEmail(clean);
        String phone = extractPhone(clean);
        String location = extractLocation(clean);

        Map<String, String> sections = extractSections(clean);
        String summary = sections.getOrDefault("SUMMARY", "");
        if (summary.isBlank() && !sections.isEmpty()) {
            summary = sections.values().iterator().next();
            if (summary.length() > 300) summary = summary.substring(0, 300) + "...";
        }

        List<ParsedSkill> extractedSkills = extractSkills(clean, sections);

        String json = "{}";
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("candidateName", candidateName);
            map.put("email", email);
            map.put("phone", phone);
            map.put("location", location);
            map.put("sectionsFound", sections.keySet());
            map.put("totalSkillsFound", extractedSkills.size());
            map.put("skills", extractedSkills);
            json = objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.warn("Failed to serialize parsed profile to JSON", e);
        }

        return ParsedResumeProfile.builder()
                .candidateName(candidateName)
                .email(email)
                .phone(phone)
                .location(location)
                .summary(summary)
                .sections(sections)
                .extractedSkills(extractedSkills)
                .parsedJson(json)
                .build();
    }

    private String extractCandidateName(String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() >= 3 && trimmed.length() <= 40 &&
                    !trimmed.toLowerCase().contains("resume") &&
                    !trimmed.toLowerCase().contains("curriculum") &&
                    !trimmed.toLowerCase().contains("http") &&
                    !trimmed.contains("@") &&
                    !trimmed.matches(".*\\d{3}.*")) {
                // Remove trailing titles like ", Ph.D." or " - Software Engineer"
                int dash = trimmed.indexOf(" - ");
                if (dash > 0) trimmed = trimmed.substring(0, dash).trim();
                return trimmed;
            }
        }
        return "Candidate Profile";
    }

    private String extractEmail(String text) {
        Matcher m = EMAIL_PATTERN.matcher(text);
        if (m.find()) {
            return m.group();
        }
        return "";
    }

    private String extractPhone(String text) {
        Matcher m = PHONE_PATTERN.matcher(text);
        if (m.find()) {
            return m.group();
        }
        return "";
    }

    private String extractLocation(String text) {
        Pattern locPat = Pattern.compile("(?i)(Dallas|Fort Worth|Austin|Houston|New York|San Francisco|Seattle|Chicago|Remote|TX|CA|NY|WA|IL)");
        Matcher m = locPat.matcher(text);
        if (m.find()) {
            return m.group();
        }
        return "Not Specified";
    }

    private Map<String, String> extractSections(String text) {
        Map<String, String> sections = new LinkedHashMap<>();
        String[] sectionHeaders = {
                "SUMMARY", "PROFESSIONAL SUMMARY", "OBJECTIVE", "ABOUT ME",
                "EXPERIENCE", "WORK EXPERIENCE", "EMPLOYMENT HISTORY", "PROFESSIONAL EXPERIENCE",
                "EDUCATION", "ACADEMIC BACKGROUND",
                "SKILLS", "TECHNICAL SKILLS", "SKILLS & TECHNOLOGIES", "CORE COMPETENCIES",
                "PROJECTS", "PERSONAL PROJECTS", "ACADEMIC PROJECTS",
                "CERTIFICATIONS", "LICENSES & CERTIFICATIONS"
        };

        String patternStr = "(?im)^\\s*(" + String.join("|", sectionHeaders) + ")\\s*[:\\-]?\\s*$";
        Pattern headerPattern = Pattern.compile(patternStr);
        Matcher matcher = headerPattern.matcher(text);

        List<Integer> startIndices = new ArrayList<>();
        List<String> headerNames = new ArrayList<>();

        while (matcher.find()) {
            startIndices.add(matcher.start());
            headerNames.add(matcher.group(1).toUpperCase(Locale.ROOT));
        }

        if (startIndices.isEmpty()) {
            sections.put("CONTENT", text);
            return sections;
        }

        for (int i = 0; i < startIndices.size(); i++) {
            int start = startIndices.get(i);
            int end = (i + 1 < startIndices.size()) ? startIndices.get(i + 1) : text.length();
            String rawHeader = headerNames.get(i);
            String canonicalHeader = mapCanonicalSectionName(rawHeader);
            String content = text.substring(start, end).replaceFirst("(?im)^\\s*" + Pattern.quote(rawHeader) + ".*\\n?", "").trim();

            if (!content.isBlank()) {
                sections.put(canonicalHeader, content);
            }
        }

        return sections;
    }

    private String mapCanonicalSectionName(String raw) {
        String u = raw.toUpperCase(Locale.ROOT);
        if (u.contains("SUMMARY") || u.contains("OBJECTIVE") || u.contains("ABOUT")) return "SUMMARY";
        if (u.contains("EXPERIENCE") || u.contains("EMPLOYMENT")) return "EXPERIENCE";
        if (u.contains("EDUCATION") || u.contains("ACADEMIC")) return "EDUCATION";
        if (u.contains("SKILL") || u.contains("COMPETENC")) return "SKILLS";
        if (u.contains("PROJECT")) return "PROJECTS";
        if (u.contains("CERTIF")) return "CERTIFICATIONS";
        return "GENERAL";
    }

    private List<ParsedSkill> extractSkills(String text, Map<String, String> sections) {
        Map<String, ParsedSkill> found = new LinkedHashMap<>();

        // Known high-frequency vocabulary list to scan
        String[] candidateTokens = {
                "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "Go", "Rust", "SQL", "HTML", "CSS", "HTML/CSS",
                "Spring Boot", "Spring", "Spring AI", "React", "Next.js", "Node.js", "Express.js", "Angular", "Vue.js", "Django", "FastAPI",
                "PostgreSQL", "Postgres", "PGVector", "MySQL", "MongoDB", "Redis", "Vector Databases", "Elasticsearch",
                "AWS", "Amazon Web Services", "Azure", "GCP", "Google Cloud", "Docker", "Kubernetes", "CI/CD", "Terraform", "Linux", "Git",
                "RAG", "Retrieval-Augmented Generation", "Machine Learning", "Artificial Intelligence", "NLP", "Embeddings", "LLM",
                "REST API", "Microservices", "GraphQL", "Kafka", "System Design", "OOP", "Unit Testing", "JUnit", "Mockito", "Bootstrap", "Tailwind CSS"
        };

        for (String candidate : candidateTokens) {
            Pattern p = Pattern.compile("(?i)\\b" + Pattern.quote(candidate) + "\\b");
            Matcher m = p.matcher(text);
            if (m.find()) {
                SkillNormalizationService.NormalizedSkillResult norm = normalizationService.normalizeSkill(candidate);
                String snippet = extractSnippetAround(text, m.start(), m.end());

                found.put(norm.getNormalizedName(), ParsedSkill.builder()
                        .rawName(candidate)
                        .canonicalName(norm.getCanonicalName())
                        .normalizedName(norm.getNormalizedName())
                        .category(norm.getCategory())
                        .contextSnippet(snippet)
                        .confidence(1.0)
                        .build());
            }
        }

        // Also inspect the SKILLS section directly for comma-separated or bulleted items
        String skillsSec = sections.get("SKILLS");
        if (skillsSec != null) {
            String[] tokens = skillsSec.split("[,;•\\|\\n]+");
            for (String tok : tokens) {
                String cleanTok = tok.trim().replaceAll("^[-*•]\\s*", "");
                if (cleanTok.length() >= 2 && cleanTok.length() <= 35 && !cleanTok.contains(":") && !cleanTok.contains("http")) {
                    SkillNormalizationService.NormalizedSkillResult norm = normalizationService.normalizeSkill(cleanTok);
                    if (!found.containsKey(norm.getNormalizedName())) {
                        found.put(norm.getNormalizedName(), ParsedSkill.builder()
                                .rawName(cleanTok)
                                .canonicalName(norm.getCanonicalName())
                                .normalizedName(norm.getNormalizedName())
                                .category(norm.getCategory())
                                .contextSnippet("Listed in Technical Skills section: " + cleanTok)
                                .confidence(0.95)
                                .build());
                    }
                }
            }
        }

        return new ArrayList<>(found.values());
    }

    private String extractSnippetAround(String text, int start, int end) {
        int left = Math.max(0, start - 40);
        int right = Math.min(text.length(), end + 40);
        String snippet = text.substring(left, right).replaceAll("\\s+", " ").trim();
        return (left > 0 ? "..." : "") + snippet + (right < text.length() ? "..." : "");
    }
}
