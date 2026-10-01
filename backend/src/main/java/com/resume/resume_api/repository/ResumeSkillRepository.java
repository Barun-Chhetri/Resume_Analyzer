package com.resume.resume_api.repository;

import com.resume.resume_api.entity.ResumeSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeSkillRepository extends JpaRepository<ResumeSkillEntity, UUID> {
    List<ResumeSkillEntity> findByResumeId(UUID resumeId);
}
