-- ==========================================================
-- Flyway Migration V2: AI Resume Intelligence Platform Schema
-- Robust schema for Resumes, Jobs, Canonical Skills, Embeddings,
-- Gap Analysis, and RAG Recommendations
-- ==========================================================

DO $$
BEGIN
    CREATE EXTENSION IF NOT EXISTS vector;
EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'vector extension not available, continuing with text/fallback';
END
$$;

-- 1. Resumes
CREATE TABLE IF NOT EXISTS resumes (
    id UUID PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    raw_text TEXT NOT NULL,
    candidate_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(100),
    location VARCHAR(255),
    summary TEXT,
    parsed_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_resumes_created_at ON resumes(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_resumes_candidate_name ON resumes(candidate_name);

-- 2. Resume Sections (for RAG chunk retrieval)
CREATE TABLE IF NOT EXISTS resume_sections (
    id UUID PRIMARY KEY,
    resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    section_type VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    embedding_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_resume_sections_resume_id ON resume_sections(resume_id);
CREATE INDEX IF NOT EXISTS idx_resume_sections_type ON resume_sections(section_type);

-- 3. Job Postings
CREATE TABLE IF NOT EXISTS job_postings (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    company VARCHAR(255),
    location VARCHAR(255),
    employment_type VARCHAR(100),
    raw_text TEXT NOT NULL,
    summary TEXT,
    required_experience_years INTEGER DEFAULT 0,
    required_degree VARCHAR(255),
    parsed_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_job_postings_title ON job_postings(title);
CREATE INDEX IF NOT EXISTS idx_job_postings_company ON job_postings(company);
CREATE INDEX IF NOT EXISTS idx_job_postings_created_at ON job_postings(created_at DESC);

-- 4. Canonical Skills & Aliases (Taxonomy & Normalization Engine)
CREATE TABLE IF NOT EXISTS canonical_skills (
    id UUID PRIMARY KEY,
    canonical_name VARCHAR(255) NOT NULL UNIQUE,
    normalized_name VARCHAR(255) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL,
    description TEXT,
    embedding_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_canonical_skills_norm ON canonical_skills(normalized_name);
CREATE INDEX IF NOT EXISTS idx_canonical_skills_category ON canonical_skills(category);

CREATE TABLE IF NOT EXISTS skill_aliases (
    id UUID PRIMARY KEY,
    canonical_skill_id UUID NOT NULL REFERENCES canonical_skills(id) ON DELETE CASCADE,
    alias VARCHAR(255) NOT NULL,
    normalized_alias VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_skill_aliases_norm ON skill_aliases(normalized_alias);
CREATE INDEX IF NOT EXISTS idx_skill_aliases_skill_id ON skill_aliases(canonical_skill_id);

-- 5. Extracted Resume Skills
CREATE TABLE IF NOT EXISTS resume_skills (
    id UUID PRIMARY KEY,
    resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    canonical_skill_id UUID REFERENCES canonical_skills(id) ON DELETE SET NULL,
    raw_name VARCHAR(255) NOT NULL,
    normalized_name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    context_snippet TEXT,
    confidence DOUBLE PRECISION DEFAULT 1.0
);

CREATE INDEX IF NOT EXISTS idx_resume_skills_resume_id ON resume_skills(resume_id);
CREATE INDEX IF NOT EXISTS idx_resume_skills_canonical_id ON resume_skills(canonical_skill_id);

-- 6. Extracted Job Skills
CREATE TABLE IF NOT EXISTS job_skills (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,
    canonical_skill_id UUID REFERENCES canonical_skills(id) ON DELETE SET NULL,
    raw_name VARCHAR(255) NOT NULL,
    normalized_name VARCHAR(255) NOT NULL,
    is_required BOOLEAN DEFAULT TRUE,
    importance VARCHAR(50) DEFAULT 'HIGH',
    category VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_job_skills_job_id ON job_skills(job_id);
CREATE INDEX IF NOT EXISTS idx_job_skills_canonical_id ON job_skills(canonical_skill_id);
CREATE INDEX IF NOT EXISTS idx_job_skills_is_required ON job_skills(is_required);

-- 7. Analyses (Resume <-> Job Multi-dimensional Matching)
CREATE TABLE IF NOT EXISTS analyses (
    id UUID PRIMARY KEY,
    resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES job_postings(id) ON DELETE CASCADE,
    overall_match_score DOUBLE PRECISION NOT NULL,
    required_skill_coverage DOUBLE PRECISION NOT NULL,
    preferred_skill_coverage DOUBLE PRECISION NOT NULL,
    semantic_similarity_score DOUBLE PRECISION NOT NULL,
    experience_alignment_score DOUBLE PRECISION NOT NULL,
    education_alignment_score DOUBLE PRECISION NOT NULL,
    summary TEXT,
    status VARCHAR(50) DEFAULT 'COMPLETED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_analyses_resume_job ON analyses(resume_id, job_id);
CREATE INDEX IF NOT EXISTS idx_analyses_created_at ON analyses(created_at DESC);

-- 8. Analysis Skill Matches (Detailed Evidence)
CREATE TABLE IF NOT EXISTS analysis_skill_matches (
    id UUID PRIMARY KEY,
    analysis_id UUID NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    resume_skill_name VARCHAR(255) NOT NULL,
    job_skill_name VARCHAR(255) NOT NULL,
    canonical_name VARCHAR(255),
    match_type VARCHAR(50) NOT NULL, -- EXACT, NORMALIZED, ALIAS, SEMANTIC_EMBEDDING
    similarity_score DOUBLE PRECISION NOT NULL,
    is_required BOOLEAN DEFAULT TRUE,
    explanation TEXT
);

CREATE INDEX IF NOT EXISTS idx_analysis_skill_matches_analysis_id ON analysis_skill_matches(analysis_id);

-- 9. Analysis Skill Gaps
CREATE TABLE IF NOT EXISTS analysis_skill_gaps (
    id UUID PRIMARY KEY,
    analysis_id UUID NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    job_skill_name VARCHAR(255) NOT NULL,
    canonical_name VARCHAR(255),
    is_required BOOLEAN DEFAULT TRUE,
    gap_severity VARCHAR(50) DEFAULT 'CRITICAL', -- CRITICAL, MODERATE, LOW
    related_evidence TEXT,
    explanation TEXT
);

CREATE INDEX IF NOT EXISTS idx_analysis_skill_gaps_analysis_id ON analysis_skill_gaps(analysis_id);

-- 10. Grounded RAG Recommendations
CREATE TABLE IF NOT EXISTS analysis_recommendations (
    id UUID PRIMARY KEY,
    analysis_id UUID NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    category VARCHAR(100) NOT NULL, -- SKILL_TO_LEARN, RESUME_EMPHASIS, PROJECT_SUGGESTION, BULLET_IMPROVEMENT, INTERVIEW_PREPARATION
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(50) DEFAULT 'HIGH', -- HIGH, MEDIUM, LOW
    actionable_steps TEXT
);

CREATE INDEX IF NOT EXISTS idx_analysis_recommendations_analysis_id ON analysis_recommendations(analysis_id);
