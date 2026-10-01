package com.resume.resume_api.service;

import com.resume.resume_api.dto.ResumeDTO;
import com.resume.resume_api.dto.ResumeDetailDTO;
import com.resume.resume_api.entity.ResumeEntity;
import com.resume.resume_api.entity.ResumeSectionEntity;
import com.resume.resume_api.exception.ResourceNotFoundException;
import com.resume.resume_api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeServiceImpl implements ResumeService {

    private final ContactRepository contactRepo;
    private final SkillRepository skillRepo;
    private final EducationRepository educationRepo;
    private final ExperienceRepository experienceRepo;
    private final ResumeRepository resumeRepository;

    @Override
    @Transactional(readOnly = true)
    public ResumeDTO getResume() {
        ResumeDTO resume = new ResumeDTO();
        resume.setContact(contactRepo.findAll().stream().findFirst().orElse(null));
        resume.setSkills(skillRepo.findAll());
        resume.setEducation(educationRepo.findAll());
        resume.setExperience(experienceRepo.findAll());
        return resume;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeDetailDTO> getAllResumes() {
        return resumeRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDetailDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeDetailDTO getResumeDetailById(UUID id) {
        ResumeEntity entity = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + id));
        return mapToDetailDTO(entity);
    }

    @Override
    @Transactional
    public void deleteResume(UUID id) {
        if (!resumeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resume not found with ID: " + id);
        }
        resumeRepository.deleteById(id);
        log.info("Deleted Resume ID: {}", id);
    }

    @Override
    @Transactional
    public ResumeDetailDTO updateCandidateName(UUID id, String candidateName) {
        ResumeEntity entity = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + id));
        entity.setCandidateName(candidateName);
        ResumeEntity saved = resumeRepository.save(entity);
        return mapToDetailDTO(saved);
    }

    private ResumeDetailDTO mapToDetailDTO(ResumeEntity entity) {
        List<ResumeDetailDTO.ExtractedSkillDTO> skillDTOs = entity.getSkills().stream()
                .map(s -> ResumeDetailDTO.ExtractedSkillDTO.builder()
                        .id(s.getId())
                        .rawName(s.getRawName())
                        .canonicalName(s.getCanonicalSkill() != null ? s.getCanonicalSkill().getCanonicalName() : s.getRawName())
                        .category(s.getCategory())
                        .contextSnippet(s.getContextSnippet())
                        .confidence(s.getConfidence())
                        .build())
                .collect(Collectors.toList());

        Map<String, String> sectionMap = new LinkedHashMap<>();
        for (ResumeSectionEntity sec : entity.getSections()) {
            sectionMap.put(sec.getSectionType(), sec.getContent());
        }

        return ResumeDetailDTO.builder()
                .id(entity.getId())
                .filename(entity.getFilename())
                .candidateName(entity.getCandidateName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .location(entity.getLocation())
                .summary(entity.getSummary())
                .fileSize(entity.getFileSize())
                .contentType(entity.getContentType())
                .rawText(entity.getRawText())
                .parsedJson(entity.getParsedJson())
                .skills(skillDTOs)
                .sections(sectionMap)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
