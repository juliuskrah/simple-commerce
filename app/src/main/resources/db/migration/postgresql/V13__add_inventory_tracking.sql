-- Add inventory tracking columns to product_variants table
ALTER TABLE product_variants
    ADD COLUMN track_inventory BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN available_quantity INT,
    ADD COLUMN low_stock_threshold INT DEFAULT 10;

-- Add constraint: if track_inventory is true, available_quantity must not be null
ALTER TABLE product_variants
    ADD CONSTRAINT check_inventory_tracking
    CHECK (
        (track_inventory = false) OR
        (track_inventory = true AND available_quantity IS NOT NULL)
    );

-- Add index for inventory queries
CREATE INDEX idx_product_variants_inventory ON product_variants(track_inventory, available_quantity)
    WHERE track_inventory = true;

-- Create inventory_adjustments table for audit trail
CREATE TABLE inventory_adjustments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    variant_id UUID NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    adjustment INT NOT NULL,
    previous_quantity INT NOT NULL,
    new_quantity INT NOT NULL,
    reason VARCHAR(255),
    adjusted_by VARCHAR(255),
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_adjustment CHECK (adjustment != 0)
);

-- Create index for inventory adjustments queries
CREATE INDEX idx_inventory_adjustments_variant ON inventory_adjustments(variant_id, created_at DESC);
CREATE INDEX idx_inventory_adjustments_created_at ON inventory_adjustments(created_at DESC);

-- Add comment
COMMENT ON TABLE inventory_adjustments IS 'Audit log of all inventory quantity changes for product variants';
COMMENT ON COLUMN inventory_adjustments.adjustment IS 'Quantity change: positive for increase, negative for decrease';
COMMENT ON COLUMN inventory_adjustments.reason IS 'Reason for adjustment: SALE, RESTOCK, CORRECTION, DAMAGE, RETURN, etc.';
