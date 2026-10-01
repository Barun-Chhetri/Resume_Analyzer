package com.resume.resume_api.repository;

import com.resume.resume_api.entity.JobPostingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPostingEntity, UUID> {
    List<JobPostingEntity> findAllByOrderByCreatedAtDesc();
}
