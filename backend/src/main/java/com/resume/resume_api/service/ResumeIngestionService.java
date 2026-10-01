package com.resume.resume_api.service;

import com.resume.resume_api.ai.EmbeddingService;
import com.resume.resume_api.dto.ResumeUploadResponse;
import com.resume.resume_api.entity.CanonicalSkillEntity;
import com.resume.resume_api.entity.ResumeEntity;
import com.resume.resume_api.entity.ResumeSectionEntity;
import com.resume.resume_api.entity.ResumeSkillEntity;
import com.resume.resume_api.exception.InvalidInputException;
import com.resume.resume_api.ingestion.DocumentParserService;
import com.resume.resume_api.ingestion.ResumeParserService;
import com.resume.resume_api.repository.CanonicalSkillRepository;
import com.resume.resume_api.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeIngestionService {

    private final DocumentParserService documentParserService;
    private final ResumeParserService resumeParserService;
    private final EmbeddingService embeddingService;
    private final ResumeRepository resumeRepository;
    private final CanonicalSkillRepository canonicalSkillRepository;

    @Transactional
    public ResumeUploadResponse ingestResumeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidInputException("File cannot be empty.");
        }

        String rawText = documentParserService.extractText(file);
        return ingestResumeText(rawText, file.getOriginalFilename(), file.getContentType(), file.getSize());
    }

    @Transactional
    public ResumeUploadResponse ingestResumeText(String rawText, String filename, String contentType, Long fileSize) {
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new InvalidInputException("Resume text cannot be blank.");
        }

        log.info("Starting ingestion pipeline for resume: {}", filename != null ? filename : "direct-text");

        ResumeParserService.ParsedResumeProfile profile = resumeParserService.parse(rawText);

        ResumeEntity resume = ResumeEntity.builder()
                .filename(filename != null ? filename : "resume-" + System.currentTimeMillis() + ".txt")
                .contentType(contentType != null ? contentType : "text/plain")
                .fileSize(fileSize != null ? fileSize : (long) rawText.length())
                .rawText(rawText)
                .candidateName(profile.getCandidateName())
                .email(profile.getEmail())
                .phone(profile.getPhone())
                .location(profile.getLocation())
                .summary(profile.getSummary())
                .parsedJson(profile.getParsedJson())
                .build();

        // 1. Process and embed sections for RAG chunk retrieval
        for (Map.Entry<String, String> sec : profile.getSections().entrySet()) {
            String secContent = sec.getValue();
            double[] embedding = embeddingService.generateEmbedding(sec.getKey() + ": " + secContent);
            String embeddingJson = embeddingService.vectorToJson(embedding);

            ResumeSectionEntity sectionEntity = ResumeSectionEntity.builder()
                    .resume(resume)
                    .sectionType(sec.getKey())
                    .content(secContent)
                    .embeddingJson(embeddingJson)
                    .build();
            resume.getSections().add(sectionEntity);
        }

        // 2. Process extracted skills
        for (ResumeParserService.ParsedSkill pSkill : profile.getExtractedSkills()) {
            Optional<CanonicalSkillEntity> canonicalOpt = canonicalSkillRepository.findByNormalizedName(pSkill.getNormalizedName());

            ResumeSkillEntity skillEntity = ResumeSkillEntity.builder()
                    .resume(resume)
                    .canonicalSkill(canonicalOpt.orElse(null))
                    .rawName(pSkill.getRawName())
                    .normalizedName(pSkill.getNormalizedName())
                    .category(pSkill.getCategory())
                    .contextSnippet(pSkill.getContextSnippet())
                    .confidence(pSkill.getConfidence())
                    .build();
            resume.getSkills().add(skillEntity);
        }

        ResumeEntity saved = resumeRepository.save(resume);
        log.info("Resume saved successfully. ID: {}, Extracted Skills: {}, Sections: {}",
                saved.getId(), saved.getSkills().size(), saved.getSections().size());

        return ResumeUploadResponse.builder()
                .id(saved.getId())
                .filename(saved.getFilename())
                .candidateName(saved.getCandidateName())
                .email(saved.getEmail())
                .totalSkillsExtracted(saved.getSkills().size())
                .totalSectionsParsed(saved.getSections().size())
                .message("Resume parsed, normalized, and embedded into PGVector successfully.")
                .build();
    }
}
