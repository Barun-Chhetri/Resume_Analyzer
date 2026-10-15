package com.resume.resume_api.repository;

import com.resume.resume_api.entity.ResumeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeRepository extends JpaRepository<ResumeEntity, UUID> {
    List<ResumeEntity> findAllByOrderByCreatedAtDesc();
    List<ResumeEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    List<ResumeEntity> findAllByUserIdIsNullOrderByCreatedAtDesc();
    Optional<ResumeEntity> findByIdAndUserId(UUID id, UUID userId);
    Optional<ResumeEntity> findByIdAndUserIdIsNull(UUID id);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
