-- Create contextual pricing system for product variants
-- This migration adds price sets and price rules for complex pricing scenarios

-- Create price_sets table
CREATE TABLE price_sets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    variant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    priority INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    CONSTRAINT fk_price_sets_variant 
        FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
);

-- Create price_rules table
CREATE TABLE price_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    price_set_id UUID NOT NULL,
    context_type VARCHAR(50) NOT NULL CHECK (context_type IN ('GEOGRAPHIC', 'CURRENCY', 'CUSTOMER_GROUP', 'DEFAULT')),
    context_value VARCHAR(255),
    price_amount DECIMAL(19,4) NOT NULL,
    price_currency VARCHAR(3) NOT NULL,
    min_quantity INTEGER,
    max_quantity INTEGER,
    valid_from TIMESTAMP WITH TIME ZONE,
    valid_until TIMESTAMP WITH TIME ZONE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    CONSTRAINT fk_price_rules_price_set 
        FOREIGN KEY (price_set_id) REFERENCES price_sets(id) ON DELETE CASCADE,
    
    -- Ensure DEFAULT context_type has no context_value
    CONSTRAINT chk_default_context_value 
        CHECK (context_type != 'DEFAULT' OR context_value IS NULL),
    
    -- Ensure quantity ranges are valid
    CONSTRAINT chk_quantity_range 
        CHECK (min_quantity IS NULL OR max_quantity IS NULL OR min_quantity <= max_quantity),
    
    -- Ensure date ranges are valid
    CONSTRAINT chk_date_range 
        CHECK (valid_from IS NULL OR valid_until IS NULL OR valid_from <= valid_until)
);

-- Create indexes for better performance
CREATE INDEX idx_price_sets_variant_id ON price_sets(variant_id);
CREATE INDEX idx_price_sets_active ON price_sets(active) WHERE active = true;
CREATE INDEX idx_price_sets_priority ON price_sets(priority);

CREATE INDEX idx_price_rules_price_set_id ON price_rules(price_set_id);
CREATE INDEX idx_price_rules_context ON price_rules(context_type, context_value);
CREATE INDEX idx_price_rules_active ON price_rules(active) WHERE active = true;
CREATE INDEX idx_price_rules_quantity ON price_rules(min_quantity, max_quantity);
CREATE INDEX idx_price_rules_validity ON price_rules(valid_from, valid_until);

-- Create a default price set for each existing variant
-- This ensures backward compatibility with existing variants
INSERT INTO price_sets (variant_id, name, priority, active)
SELECT 
    id,
    'Default Price Set',
    0,
    true
FROM product_variants
WHERE price_amount IS NOT NULL AND price_currency IS NOT NULL;

-- Create default price rules for existing variant prices
INSERT INTO price_rules (price_set_id, context_type, price_amount, price_currency, active)
SELECT 
    ps.id,
    'DEFAULT',
    pv.price_amount,
    pv.price_currency,
    true
FROM price_sets ps
JOIN product_variants pv ON ps.variant_id = pv.id
WHERE pv.price_amount IS NOT NULL AND pv.price_currency IS NOT NULL;

-- Add comment to explain the purpose
COMMENT ON TABLE price_sets IS 'Contextual pricing sets for product variants supporting geographic, currency, customer group, time-based, and quantity-based pricing';
COMMENT ON TABLE price_rules IS 'Individual pricing rules within a price set, defining specific pricing contexts and constraints';

COMMENT ON COLUMN price_rules.context_type IS 'Type of pricing context: GEOGRAPHIC, CURRENCY, CUSTOMER_GROUP, or DEFAULT';
COMMENT ON COLUMN price_rules.context_value IS 'Specific value for the context (e.g., "US" for GEOGRAPHIC, "USD" for CURRENCY, "B2B" for CUSTOMER_GROUP)';
COMMENT ON COLUMN price_rules.min_quantity IS 'Minimum quantity for this pricing rule (for tiered pricing)';
COMMENT ON COLUMN price_rules.max_quantity IS 'Maximum quantity for this pricing rule (for tiered pricing)';
COMMENT ON COLUMN price_rules.valid_from IS 'Start date/time for this pricing rule (for time-based pricing)';
COMMENT ON COLUMN price_rules.valid_until IS 'End date/time for this pricing rule (for time-based pricing)';