package com.resume.resume_api.controller;

import com.resume.resume_api.dto.ResumeDTO;
import com.resume.resume_api.dto.ResumeDetailDTO;
import com.resume.resume_api.dto.ResumeUploadResponse;
import com.resume.resume_api.service.ResumeIngestionService;
import com.resume.resume_api.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173"})
@Tag(name = "Resume Management", description = "Endpoints for uploading, parsing, inspecting, and managing resumes")
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeIngestionService ingestionService;

    // --- Legacy Single-Resume Endpoint (Preserves original portfolio functionality) ---
    @GetMapping("/api/resume")
    @Operation(summary = "Get default resume profile (Legacy endpoint)")
    public ResumeDTO getResume() {
        return resumeService.getResume();
    }

    // --- AI Resume Intelligence Platform Endpoints ---

    @PostMapping(value = "/api/resumes/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and parse resume file (PDF, DOCX, TXT)")
    public ResponseEntity<ResumeUploadResponse> uploadResumeFile(@RequestParam("file") MultipartFile file) {
        ResumeUploadResponse response = ingestionService.ingestResumeFile(file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/api/resumes")
    @Operation(summary = "Ingest resume from raw text")
    public ResponseEntity<ResumeUploadResponse> ingestResumeText(@RequestBody Map<String, String> body) {
        String text = body.get("rawText");
        String filename = body.getOrDefault("filename", "pasted-resume.txt");
        ResumeUploadResponse response = ingestionService.ingestResumeText(text, filename, "text/plain", (long) (text != null ? text.length() : 0));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/resumes")
    @Operation(summary = "List all ingested resumes")
    public ResponseEntity<List<ResumeDetailDTO>> getAllResumes() {
        List<ResumeDetailDTO> list = resumeService.getAllResumes();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/api/resumes/{id}")
    @Operation(summary = "Get resume details by ID")
    public ResponseEntity<ResumeDetailDTO> getResumeById(@PathVariable UUID id) {
        ResumeDetailDTO dto = resumeService.getResumeDetailById(id);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/api/resumes/{id}")
    @Operation(summary = "Delete resume by ID")
    public ResponseEntity<Void> deleteResume(@PathVariable UUID id) {
        resumeService.deleteResume(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/resumes/{id}/name")
    @Operation(summary = "Update candidate name on resume")
    public ResponseEntity<ResumeDetailDTO> updateCandidateName(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String name = body.get("candidateName");
        ResumeDetailDTO updated = resumeService.updateCandidateName(id, name);
        return ResponseEntity.ok(updated);
    }
}
