package com.resume.resume_api.controller;

import com.resume.resume_api.ai.RagRetrievalService;
import com.resume.resume_api.dto.CanonicalSkillDTO;
import com.resume.resume_api.entity.CanonicalSkillEntity;
import com.resume.resume_api.entity.SkillAliasEntity;
import com.resume.resume_api.normalization.SkillNormalizationService;
import com.resume.resume_api.repository.CanonicalSkillRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173"})
@Tag(name = "Skill Intelligence Explorer", description = "Endpoints for exploring normalized canonical skills, aliases, and semantic skill search")
public class SkillExplorerController {

    private final CanonicalSkillRepository canonicalSkillRepo;
    private final SkillNormalizationService normalizationService;
    private final RagRetrievalService ragRetrievalService;

    @GetMapping("/api/skills/canonical")
    @Operation(summary = "List all canonical skills with optional category or query filter")
    public ResponseEntity<List<CanonicalSkillDTO>> getCanonicalSkills(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "query", required = false) String query) {

        List<CanonicalSkillEntity> list;
        if (query != null && !query.isBlank()) {
            list = canonicalSkillRepo.searchByNameOrDescription(query.trim());
        } else if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            list = canonicalSkillRepo.findByCategory(category.trim());
        } else {
            list = canonicalSkillRepo.findAll();
        }

        List<CanonicalSkillDTO> dtos = list.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/api/skills/categories")
    @Operation(summary = "List all distinct skill categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = canonicalSkillRepo.findDistinctCategories();
        if (categories.isEmpty()) {
            categories = List.of("Languages", "Frameworks", "Databases", "Cloud & DevOps", "AI/ML", "Architecture", "Testing", "Methodologies");
        }
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/api/search/skills")
    @Operation(summary = "Search skills with normalization and semantic vector similarity")
    public ResponseEntity<Map<String, Object>> searchSkills(@RequestBody Map<String, String> body) {
        String query = body.getOrDefault("query", "").trim();
        if (query.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Query cannot be empty"));
        }

        // 1. Normalization & Alias lookup
        SkillNormalizationService.NormalizedSkillResult normResult = normalizationService.normalizeSkill(query);

        // 2. Vector semantic search for related/similar skills
        List<RagRetrievalService.RetrievedDocument> similarSkills = ragRetrievalService.retrieveSimilarSkills(query, 0.45);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", query);
        response.put("normalized", normResult);
        response.put("semanticMatches", similarSkills);

        return ResponseEntity.ok(response);
    }

    private CanonicalSkillDTO mapToDTO(CanonicalSkillEntity s) {
        List<String> aliasList = s.getAliases() != null ?
                s.getAliases().stream().map(SkillAliasEntity::getAlias).collect(Collectors.toList()) :
                Collections.emptyList();

        return CanonicalSkillDTO.builder()
                .id(s.getId())
                .canonicalName(s.getCanonicalName())
                .normalizedName(s.getNormalizedName())
                .category(s.getCategory())
                .description(s.getDescription())
                .aliases(aliasList)
                .build();
    }
}
