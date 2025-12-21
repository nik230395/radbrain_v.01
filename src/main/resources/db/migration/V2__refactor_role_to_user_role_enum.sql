-- Migration script for refactoring from Role entity to UserRole enum
-- Version: V2__refactor_role_to_user_role_enum.sql
-- Date: 2025-12-21
-- Description: Replace Role entity and user_role join table with UserRole enum column

-- Step 1: Add new user_role column to users table
ALTER TABLE users ADD COLUMN user_role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Step 2: Migrate existing data from user_role join table to new column
-- Set ADMIN role for users who have ROLE_ADMIN
UPDATE users u
SET u.user_role = 'ADMIN'
WHERE EXISTS (
    SELECT 1 FROM user_role ur
    INNER JOIN role r ON ur.role_id = r.id
    WHERE ur.user_id = u.id AND r.name = 'ROLE_ADMIN'
);

-- Note: All other users will already have 'USER' role from the DEFAULT value

-- Step 3: Drop old role column from users table (if it exists)
-- ALTER TABLE users DROP COLUMN IF EXISTS role;

-- Step 4: Drop user_role join table
DROP TABLE IF EXISTS user_role;

-- Step 5: Drop role table
DROP TABLE IF EXISTS role;

-- Notes:
-- - The new user_role column uses enum values: 'USER', 'ADMIN'
-- - Default value is 'USER' for all new users
-- - The migration preserves existing role assignments
-- - Consider backing up your database before running this migration
