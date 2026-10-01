package com.resume.resume_api.repository;

import com.resume.resume_api.entity.AnalysisRecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnalysisRecommendationRepository extends JpaRepository<AnalysisRecommendationEntity, UUID> {
    List<AnalysisRecommendationEntity> findByAnalysisId(UUID analysisId);
}
