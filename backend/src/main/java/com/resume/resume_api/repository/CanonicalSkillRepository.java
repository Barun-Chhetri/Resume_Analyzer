package com.resume.resume_api.repository;

import com.resume.resume_api.entity.CanonicalSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CanonicalSkillRepository extends JpaRepository<CanonicalSkillEntity, UUID> {
    Optional<CanonicalSkillEntity> findByNormalizedName(String normalizedName);
    Optional<CanonicalSkillEntity> findByCanonicalNameIgnoreCase(String canonicalName);
    List<CanonicalSkillEntity> findByCategory(String category);

    @Query("SELECT s FROM CanonicalSkillEntity s WHERE LOWER(s.canonicalName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<CanonicalSkillEntity> searchByNameOrDescription(@Param("query") String query);

    @Query("SELECT DISTINCT s.category FROM CanonicalSkillEntity s")
    List<String> findDistinctCategories();
}
