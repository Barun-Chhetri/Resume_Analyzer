package com.resume.resume_api.repository;

import com.resume.resume_api.entity.JobPostingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPostingEntity, UUID> {
    List<JobPostingEntity> findAllByOrderByCreatedAtDesc();
    List<JobPostingEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    List<JobPostingEntity> findAllByUserIdIsNullOrderByCreatedAtDesc();
    Optional<JobPostingEntity> findByIdAndUserId(UUID id, UUID userId);
    Optional<JobPostingEntity> findByIdAndUserIdIsNull(UUID id);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
