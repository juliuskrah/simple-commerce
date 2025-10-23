-- Add status column to products table
ALTER TABLE IF EXISTS products
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- Add constraint to ensure valid status values
ALTER TABLE IF EXISTS products
ADD CONSTRAINT products_status_check 
CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'));

-- Update existing products to have DRAFT status (since they're all null)
UPDATE products SET status = 'DRAFT' WHERE status IS NULL;
