-- ==========================================================
-- Flyway Migration V1: Existing Schema Initialization
-- Preserves legacy Contact, Skill, Education, Experience entities
-- ==========================================================

CREATE TABLE IF NOT EXISTS contact (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    title VARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255),
    website VARCHAR(255),
    address VARCHAR(255),
    summary TEXT
);

CREATE TABLE IF NOT EXISTS skill (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS education (
    id BIGSERIAL PRIMARY KEY,
    degree VARCHAR(255) NOT NULL,
    school VARCHAR(255) NOT NULL,
    year VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS experience (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    period VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS experience_achievements (
    experience_id BIGINT NOT NULL REFERENCES experience(id) ON DELETE CASCADE,
    achievements TEXT
);

-- Seed Barun Chhetri's default contact profile for the legacy /api/resume endpoint
INSERT INTO contact (name, title, phone, email, website, address, summary)
VALUES (
    'Barun Chhetri',
    'Fullstack Developer & AI Engineer',
    '+1 (555) 019-2834',
    'barun.chhetri@example.com',
    'https://github.com/barunchhetri',
    'Dallas-Fort Worth, TX',
    'Passionate Software Engineer skilled in Java, Spring Boot, Spring AI, PostgreSQL, and scalable web architectures.'
) ON CONFLICT DO NOTHING;

-- Seed default skills
INSERT INTO skill (name) VALUES 
('Java'), ('Spring Boot'), ('PostgreSQL'), ('React'), ('Docker'), ('REST APIs'), ('Git')
ON CONFLICT DO NOTHING;

-- Seed default education
INSERT INTO education (degree, school, year) VALUES 
('B.S. in Computer Science', 'University of North Texas', '2023 - 2026')
ON CONFLICT DO NOTHING;

-- Seed default experience
INSERT INTO experience (title, company, period) VALUES 
('Software Engineering Intern', 'Tech Innovation Labs', 'Summer 2025')
ON CONFLICT DO NOTHING;
