-- Refactor users table to staff table and add actor type support
ALTER TABLE users RENAME TO staff;

-- Add new columns for staff-specific fields
ALTER TABLE staff ADD COLUMN actor_type VARCHAR(20) NOT NULL DEFAULT 'STAFF';
ALTER TABLE staff ADD COLUMN department VARCHAR(100);
ALTER TABLE staff ADD COLUMN role VARCHAR(50);

-- Update any existing data to have STAFF actor type
UPDATE staff SET actor_type = 'STAFF' WHERE actor_type IS NULL;