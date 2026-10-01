package com.resume.resume_api.repository;

import com.resume.resume_api.entity.AnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnalysisRepository extends JpaRepository<AnalysisEntity, UUID> {
    List<AnalysisEntity> findAllByOrderByCreatedAtDesc();
    List<AnalysisEntity> findByResumeIdOrderByCreatedAtDesc(UUID resumeId);
    List<AnalysisEntity> findByJobPostingIdOrderByCreatedAtDesc(UUID jobId);
}
