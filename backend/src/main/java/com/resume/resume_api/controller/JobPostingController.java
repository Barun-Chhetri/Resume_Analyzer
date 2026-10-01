package com.resume.resume_api.controller;

import com.resume.resume_api.dto.JobPostingDTO;
import com.resume.resume_api.dto.JobPostingRequest;
import com.resume.resume_api.service.JobPostingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173"})
@Tag(name = "Job Management", description = "Endpoints for creating, uploading, and managing job postings")
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @PostMapping
    @Operation(summary = "Create a job posting from text")
    public ResponseEntity<JobPostingDTO> createJob(@Valid @RequestBody JobPostingRequest request) {
        JobPostingDTO created = jobPostingService.createJobPosting(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and parse job description document (PDF, DOCX, TXT)")
    public ResponseEntity<JobPostingDTO> uploadJobDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "company", required = false) String company) {
        JobPostingDTO created = jobPostingService.uploadJobPostingDocument(file, title, company);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List all job postings")
    public ResponseEntity<List<JobPostingDTO>> getAllJobs() {
        return ResponseEntity.ok(jobPostingService.getAllJobPostings());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job posting by ID")
    public ResponseEntity<JobPostingDTO> getJobById(@PathVariable UUID id) {
        return ResponseEntity.ok(jobPostingService.getJobPostingById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete job posting by ID")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID id) {
        jobPostingService.deleteJobPosting(id);
        return ResponseEntity.noContent().build();
    }
}
