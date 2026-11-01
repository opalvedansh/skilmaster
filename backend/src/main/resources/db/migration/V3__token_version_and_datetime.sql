-- Bumped on logout and password change; tokens carrying an older version are rejected.
ALTER TABLE users ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0;

-- DATETIME instead of TIMESTAMP: no year-2038 limit and no session-time-zone conversion.
ALTER TABLE users MODIFY created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE question_attempts MODIFY attempted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
