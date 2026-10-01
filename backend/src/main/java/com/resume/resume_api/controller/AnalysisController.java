package com.resume.resume_api.controller;

import com.resume.resume_api.dto.AnalysisRequest;
import com.resume.resume_api.dto.AnalysisResponseDTO;
import com.resume.resume_api.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173"})
@Tag(name = "Analysis & Matching", description = "Endpoints for analyzing resumes against job descriptions, skill matching, gap analysis, and RAG recommendations")
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    @Operation(summary = "Execute analysis between a resume and a job posting")
    public ResponseEntity<AnalysisResponseDTO> runAnalysis(@Valid @RequestBody AnalysisRequest request) {
        AnalysisResponseDTO response = analysisService.runAnalysis(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List all previous analyses")
    public ResponseEntity<List<AnalysisResponseDTO>> getAllAnalyses() {
        return ResponseEntity.ok(analysisService.getAllAnalyses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full analysis results by ID")
    public ResponseEntity<AnalysisResponseDTO> getAnalysisById(@PathVariable UUID id) {
        return ResponseEntity.ok(analysisService.getAnalysisById(id));
    }

    @GetMapping("/{id}/skills")
    @Operation(summary = "Get matched skills evidence for an analysis")
    public ResponseEntity<List<AnalysisResponseDTO.SkillMatchDTO>> getAnalysisSkills(@PathVariable UUID id) {
        return ResponseEntity.ok(analysisService.getAnalysisSkills(id));
    }

    @GetMapping("/{id}/gaps")
    @Operation(summary = "Get identified skill gaps for an analysis")
    public ResponseEntity<List<AnalysisResponseDTO.SkillGapDTO>> getAnalysisGaps(@PathVariable UUID id) {
        return ResponseEntity.ok(analysisService.getAnalysisGaps(id));
    }

    @GetMapping("/{id}/recommendations")
    @Operation(summary = "Get grounded AI recommendations for an analysis")
    public ResponseEntity<List<AnalysisResponseDTO.RecommendationDTO>> getAnalysisRecommendations(@PathVariable UUID id) {
        return ResponseEntity.ok(analysisService.getAnalysisRecommendations(id));
    }
}
