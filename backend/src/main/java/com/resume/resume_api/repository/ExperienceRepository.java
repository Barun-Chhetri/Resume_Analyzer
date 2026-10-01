package com.resume.resume_api.repository;


import com.resume.resume_api.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {}
