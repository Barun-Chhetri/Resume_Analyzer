package com.resume.resume_api.normalization;

import java.util.Locale;

public final class TextNormalizer {

    private TextNormalizer() {
        // utility class
    }

    /**
     * Normalizes a skill or entity name into a canonical key.
     * Examples:
     * "Spring Boot" -> "springboot"
     * "C++" -> "cpp"
     * "C#" -> "csharp"
     * ".NET" -> "dotnet"
     * "Node.js" -> "nodejs"
     * "Next.js" -> "nextjs"
     * "CI/CD" -> "cicd"
     * "PostgreSQL" -> "postgresql"
     */
    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String trimmed = input.trim().toLowerCase(Locale.ROOT);

        // Specific symbol replacements before stripping
        trimmed = trimmed.replace("c++", "cpp")
                         .replace("c#", "csharp")
                         .replace(".net", "dotnet")
                         .replace("node.js", "nodejs")
                         .replace("next.js", "nextjs")
                         .replace("vue.js", "vuejs")
                         .replace("react.js", "react")
                         .replace("ci/cd", "cicd")
                         .replace("k8s", "kubernetes");

        // Remove all non-alphanumeric characters
        return trimmed.replaceAll("[^a-z0-9]", "");
    }

    /**
     * Cleans text for NLP and embedding extraction.
     */
    public static String cleanText(String text) {
        if (text == null) {
            return "";
        }
        // Normalize newlines, replace tabs, strip non-printable characters
        return text.replaceAll("\\r\\n", "\n")
                   .replaceAll("\\r", "\n")
                   .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "")
                   .replaceAll("[ \\t]+", " ")
                   .trim();
    }
}
