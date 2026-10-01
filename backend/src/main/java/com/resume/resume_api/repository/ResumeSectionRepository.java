package com.resume.resume_api.repository;

import com.resume.resume_api.entity.ResumeSectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeSectionRepository extends JpaRepository<ResumeSectionEntity, UUID> {
    List<ResumeSectionEntity> findByResumeId(UUID resumeId);
    List<ResumeSectionEntity> findByResumeIdAndSectionType(UUID resumeId, String sectionType);
}
