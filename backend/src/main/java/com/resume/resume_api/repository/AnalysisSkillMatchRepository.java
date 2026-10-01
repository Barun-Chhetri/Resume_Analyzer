package com.resume.resume_api.repository;

import com.resume.resume_api.entity.AnalysisSkillMatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnalysisSkillMatchRepository extends JpaRepository<AnalysisSkillMatchEntity, UUID> {
    List<AnalysisSkillMatchEntity> findByAnalysisId(UUID analysisId);
}
