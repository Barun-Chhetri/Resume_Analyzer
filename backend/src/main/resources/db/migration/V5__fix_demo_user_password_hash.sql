-- ==========================================================
-- Flyway Migration V5: Fix Demo User Password Hash
-- ==========================================================

UPDATE users
SET password_hash = '65536:759a831846f1f6af0ba0431ea232f544:817a11703af8f3b4f5c97a1e559566546198dd49bebb8b52375e8a1240a92650',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'demo@resumex.ai';
