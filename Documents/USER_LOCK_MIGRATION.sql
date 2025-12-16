-- Lock/Unlock User Feature Migration
-- Run this SQL to add the lock functionality columns to existing users table

-- Add is_locked column with default value false
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_locked BOOLEAN NOT NULL DEFAULT FALSE;

-- Add locked_at column for tracking when user was locked
ALTER TABLE users ADD COLUMN IF NOT EXISTS locked_at TIMESTAMP NULL;

-- Add lock_reason column for storing the reason for locking
ALTER TABLE users ADD COLUMN IF NOT EXISTS lock_reason VARCHAR(500) NULL;

-- For MySQL syntax (if the above doesn't work):
-- ALTER TABLE users ADD COLUMN is_locked TINYINT(1) NOT NULL DEFAULT 0;
-- ALTER TABLE users ADD COLUMN locked_at TIMESTAMP NULL;
-- ALTER TABLE users ADD COLUMN lock_reason VARCHAR(500) NULL;

