-- ==========================================================
-- Flyway Migration V4: User Authentication & Scoped Sessions
-- ==========================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- 2. Associate Resumes with User
ALTER TABLE resumes ADD COLUMN IF NOT EXISTS user_id UUID REFERENCES users(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_resumes_user_id ON resumes(user_id);

-- 3. Associate Job Postings with User
ALTER TABLE job_postings ADD COLUMN IF NOT EXISTS user_id UUID REFERENCES users(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_job_postings_user_id ON job_postings(user_id);

-- 4. Associate Analyses with User
ALTER TABLE analyses ADD COLUMN IF NOT EXISTS user_id UUID REFERENCES users(id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_analyses_user_id ON analyses(user_id);

-- 5. Seed default demo user (Password: Demo1234!)
-- Salted PBKDF2-HMAC-SHA256 hash for 'Demo1234!'
INSERT INTO users (id, name, email, password_hash, created_at, updated_at)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'Demo User',
    'demo@resumex.ai',
    '65536:7b5d19e078ba19253ecf30b92ea9ec71:92ec29b6f849007f5d471550c6065eeae568df565e3cb7666ee3cf37419e71ec',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- 6. Clean legacy single-portfolio contact record
DELETE FROM contact WHERE email = 'barun.chhetri@example.com';
