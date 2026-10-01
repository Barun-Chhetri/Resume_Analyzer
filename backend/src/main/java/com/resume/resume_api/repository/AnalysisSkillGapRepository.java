package com.resume.resume_api.repository;

import com.resume.resume_api.entity.AnalysisSkillGapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnalysisSkillGapRepository extends JpaRepository<AnalysisSkillGapEntity, UUID> {
    List<AnalysisSkillGapEntity> findByAnalysisId(UUID analysisId);
}
