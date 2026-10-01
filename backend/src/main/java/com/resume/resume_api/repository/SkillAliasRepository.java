package com.resume.resume_api.repository;

import com.resume.resume_api.entity.SkillAliasEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillAliasRepository extends JpaRepository<SkillAliasEntity, UUID> {
    Optional<SkillAliasEntity> findByNormalizedAlias(String normalizedAlias);

    @Query("SELECT a FROM SkillAliasEntity a WHERE LOWER(a.alias) = LOWER(:alias)")
    Optional<SkillAliasEntity> findByAliasIgnoreCase(@Param("alias") String alias);

    List<SkillAliasEntity> findByCanonicalSkillId(UUID canonicalSkillId);
}
