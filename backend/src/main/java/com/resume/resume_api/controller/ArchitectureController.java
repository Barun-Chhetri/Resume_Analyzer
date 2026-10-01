package com.resume.resume_api.controller;

import com.resume.resume_api.ai.EmbeddingService;
import com.resume.resume_api.dto.ArchitectureInfoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/architecture")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173"})
@Tag(name = "System Architecture", description = "Endpoints exposing system design metadata, vector database configuration, and RAG pipeline diagnostics")
public class ArchitectureController {

    private final EmbeddingService embeddingService;

    @Value("${spring.application.name:resume-intelligence-platform}")
    private String appName;

    @Value("${app.ai.vector-dimension:1536}")
    private int vectorDimension;

    @GetMapping
    @Operation(summary = "Get system architectural specifications and pipeline status")
    public ResponseEntity<ArchitectureInfoDTO> getArchitectureInfo() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long heapMax = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("heapUsedMb", heapUsed);
        metrics.put("heapMaxMb", heapMax);
        metrics.put("uptimeSeconds", uptimeSeconds);
        metrics.put("availableProcessors", Runtime.getRuntime().availableProcessors());

        ArchitectureInfoDTO info = ArchitectureInfoDTO.builder()
                .applicationName(appName)
                .version("1.0.0-PROD")
                .javaVersion(System.getProperty("java.version"))
                .springBootVersion("3.4.1")
                .activeEmbeddingModel(embeddingService.getActiveProvider())
                .vectorDimensions(vectorDimension)
                .databaseDialect("PostgreSQL + PGVector")
                .pgvectorEnabled(true)
                .localFallbackEnabled(true)
                .supportedDocumentFormats(List.of("PDF (.pdf)", "Microsoft Word (.docx)", "Plain Text (.txt)", "Markdown (.md)"))
                .ragPipelineStages(List.of(
                        "1. Document Ingestion (Apache PDFBox / Apache POI)",
                        "2. Text Normalization & Section Chunking",
                        "3. Skill Taxonomy Mapping & Entity Normalization",
                        "4. 1536-Dimensional Dense Vector Embedding Generation",
                        "5. PGVector / Database Vector Persistence",
                        "6. Multi-Factor Semantic & Alias Matching",
                        "7. Skill-Gap & Adjacent Domain Analysis",
                        "8. RAG Context Construction & Evidence Retrieval",
                        "9. Grounded, Zero-Hallucination Recommendation Synthesis"
                ))
                .systemMetrics(metrics)
                .build();

        return ResponseEntity.ok(info);
    }
}
