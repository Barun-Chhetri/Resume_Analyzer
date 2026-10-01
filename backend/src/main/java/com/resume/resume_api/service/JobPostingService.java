package com.resume.resume_api.service;

import com.resume.resume_api.dto.JobPostingDTO;
import com.resume.resume_api.dto.JobPostingRequest;
import com.resume.resume_api.entity.CanonicalSkillEntity;
import com.resume.resume_api.entity.JobPostingEntity;
import com.resume.resume_api.entity.JobSkillEntity;
import com.resume.resume_api.exception.InvalidInputException;
import com.resume.resume_api.exception.ResourceNotFoundException;
import com.resume.resume_api.ingestion.DocumentParserService;
import com.resume.resume_api.ingestion.JobDescriptionParserService;
import com.resume.resume_api.repository.CanonicalSkillRepository;
import com.resume.resume_api.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final JobDescriptionParserService jobParserService;
    private final DocumentParserService documentParserService;
    private final CanonicalSkillRepository canonicalSkillRepository;

    @Transactional
    public JobPostingDTO createJobPosting(JobPostingRequest req) {
        if (req == null || req.getRawText() == null || req.getRawText().isBlank()) {
            throw new InvalidInputException("Job description raw text cannot be blank.");
        }

        JobDescriptionParserService.ParsedJobProfile profile =
                jobParserService.parse(req.getTitle(), req.getCompany(), req.getRawText());

        JobPostingEntity job = JobPostingEntity.builder()
                .title(profile.getTitle())
                .company(profile.getCompany())
                .location(req.getLocation() != null && !req.getLocation().isBlank() ? req.getLocation() : profile.getLocation())
                .employmentType(req.getEmploymentType() != null && !req.getEmploymentType().isBlank() ? req.getEmploymentType() : profile.getEmploymentType())
                .rawText(req.getRawText())
                .summary(profile.getSummary())
                .requiredExperienceYears(profile.getRequiredExperienceYears())
                .requiredDegree(profile.getRequiredDegree())
                .parsedJson(profile.getParsedJson())
                .build();

        for (JobDescriptionParserService.ParsedJobSkill pSkill : profile.getExtractedSkills()) {
            Optional<CanonicalSkillEntity> canonicalOpt = canonicalSkillRepository.findByNormalizedName(pSkill.getNormalizedName());

            JobSkillEntity skillEntity = JobSkillEntity.builder()
                    .jobPosting(job)
                    .canonicalSkill(canonicalOpt.orElse(null))
                    .rawName(pSkill.getRawName())
                    .normalizedName(pSkill.getNormalizedName())
                    .isRequired(pSkill.isRequired())
                    .importance(pSkill.getImportance())
                    .category(pSkill.getCategory())
                    .build();

            job.getSkills().add(skillEntity);
        }

        JobPostingEntity saved = jobPostingRepository.save(job);
        log.info("Created Job Posting ID: {}, Title: {}, Skills Extracted: {}",
                saved.getId(), saved.getTitle(), saved.getSkills().size());

        return mapToDTO(saved);
    }

    @Transactional
    public JobPostingDTO uploadJobPostingDocument(MultipartFile file, String title, String company) {
        String rawText = documentParserService.extractText(file);
        JobPostingRequest req = JobPostingRequest.builder()
                .title(title)
                .company(company)
                .rawText(rawText)
                .build();
        return createJobPosting(req);
    }

    @Transactional(readOnly = true)
    public List<JobPostingDTO> getAllJobPostings() {
        return jobPostingRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobPostingDTO getJobPostingById(UUID id) {
        JobPostingEntity job = jobPostingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found with ID: " + id));
        return mapToDTO(job);
    }

    @Transactional
    public void deleteJobPosting(UUID id) {
        if (!jobPostingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Job posting not found with ID: " + id);
        }
        jobPostingRepository.deleteById(id);
        log.info("Deleted Job Posting ID: {}", id);
    }

    private JobPostingDTO mapToDTO(JobPostingEntity job) {
        List<JobPostingDTO.JobSkillDTO> skillDTOs = job.getSkills().stream()
                .map(s -> JobPostingDTO.JobSkillDTO.builder()
                        .id(s.getId())
                        .rawName(s.getRawName())
                        .canonicalName(s.getCanonicalSkill() != null ? s.getCanonicalSkill().getCanonicalName() : s.getRawName())
                        .category(s.getCategory())
                        .isRequired(s.getIsRequired())
                        .importance(s.getImportance())
                        .build())
                .collect(Collectors.toList());

        return JobPostingDTO.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .employmentType(job.getEmploymentType())
                .summary(job.getSummary())
                .requiredExperienceYears(job.getRequiredExperienceYears())
                .requiredDegree(job.getRequiredDegree())
                .rawText(job.getRawText())
                .parsedJson(job.getParsedJson())
                .skills(skillDTOs)
                .createdAt(job.getCreatedAt())
                .build();
    }
}
