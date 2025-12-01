-- Create discounts table
CREATE TABLE discounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,

    -- Discount type and value
    discount_type VARCHAR(50) NOT NULL,
    value_amount DECIMAL(19, 4),
    value_percentage DECIMAL(5, 2),

    -- Application rules
    applies_to VARCHAR(50) NOT NULL DEFAULT 'ORDER',
    minimum_order_amount DECIMAL(19, 4),
    minimum_quantity INT,

    -- Usage limits
    usage_limit INT,
    usage_count INT NOT NULL DEFAULT 0,
    per_customer_limit INT,

    -- Date restrictions
    starts_at TIMESTAMP(6) WITH TIME ZONE,
    ends_at TIMESTAMP(6) WITH TIME ZONE,

    -- Status
    active BOOLEAN NOT NULL DEFAULT true,

    -- Metadata
    created_by VARCHAR(250),
    metadata JSONB,

    -- Timestamps
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_discount_type CHECK (
        discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT', 'FREE_SHIPPING', 'BUY_X_GET_Y')
    ),
    CONSTRAINT check_discount_value CHECK (
        (discount_type = 'PERCENTAGE' AND value_percentage IS NOT NULL AND value_percentage > 0 AND value_percentage <= 100) OR
        (discount_type = 'FIXED_AMOUNT' AND value_amount IS NOT NULL AND value_amount > 0) OR
        (discount_type = 'FREE_SHIPPING') OR
        (discount_type = 'BUY_X_GET_Y')
    ),
    CONSTRAINT check_applies_to CHECK (
        applies_to IN ('ORDER', 'PRODUCT', 'CATEGORY', 'SHIPPING')
    )
);

-- Create discount_products table (products eligible for discount)
CREATE TABLE discount_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    discount_id UUID NOT NULL REFERENCES discounts(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_discount_product UNIQUE (discount_id, product_id)
);

-- Create discount_categories table (categories eligible for discount)
CREATE TABLE discount_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    discount_id UUID NOT NULL REFERENCES discounts(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_discount_category UNIQUE (discount_id, category_id)
);

-- Create discount_usages table (track customer usage)
CREATE TABLE discount_usages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    discount_id UUID NOT NULL REFERENCES discounts(id) ON DELETE CASCADE,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    discount_amount DECIMAL(19, 4) NOT NULL,
    discount_currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_discount_order UNIQUE (discount_id, order_id)
);

-- Create automatic_discounts table (discounts that apply automatically without codes)
CREATE TABLE automatic_discounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,

    -- Discount type and value
    discount_type VARCHAR(50) NOT NULL,
    value_amount DECIMAL(19, 4),
    value_percentage DECIMAL(5, 2),

    -- Priority (higher numbers = higher priority)
    priority INT NOT NULL DEFAULT 0,

    -- Conditions
    minimum_order_amount DECIMAL(19, 4),
    minimum_quantity INT,
    customer_group VARCHAR(100),

    -- Date restrictions
    starts_at TIMESTAMP(6) WITH TIME ZONE,
    ends_at TIMESTAMP(6) WITH TIME ZONE,

    -- Status
    active BOOLEAN NOT NULL DEFAULT true,

    -- Metadata
    created_by VARCHAR(250),
    metadata JSONB,

    -- Timestamps
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_automatic_discount_type CHECK (
        discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT', 'FREE_SHIPPING')
    ),
    CONSTRAINT check_automatic_discount_value CHECK (
        (discount_type = 'PERCENTAGE' AND value_percentage IS NOT NULL AND value_percentage > 0 AND value_percentage <= 100) OR
        (discount_type = 'FIXED_AMOUNT' AND value_amount IS NOT NULL AND value_amount > 0) OR
        (discount_type = 'FREE_SHIPPING')
    )
);

-- Create indexes
CREATE INDEX idx_discounts_code ON discounts(code);
CREATE INDEX idx_discounts_active ON discounts(active) WHERE active = true;
CREATE INDEX idx_discounts_dates ON discounts(starts_at, ends_at);
CREATE INDEX idx_discount_products_discount_id ON discount_products(discount_id);
CREATE INDEX idx_discount_products_product_id ON discount_products(product_id);
CREATE INDEX idx_discount_categories_discount_id ON discount_categories(discount_id);
CREATE INDEX idx_discount_categories_category_id ON discount_categories(category_id);
CREATE INDEX idx_discount_usages_discount_id ON discount_usages(discount_id);
CREATE INDEX idx_discount_usages_customer_id ON discount_usages(customer_id);
CREATE INDEX idx_discount_usages_order_id ON discount_usages(order_id);
CREATE INDEX idx_automatic_discounts_active ON automatic_discounts(active) WHERE active = true;
CREATE INDEX idx_automatic_discounts_priority ON automatic_discounts(priority DESC);

-- Create triggers for updated_at
CREATE TRIGGER discount_updated_at_trigger
    BEFORE UPDATE ON discounts
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_updated_at();

CREATE TRIGGER automatic_discount_updated_at_trigger
    BEFORE UPDATE ON automatic_discounts
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_updated_at();

-- Create function to increment discount usage
CREATE OR REPLACE FUNCTION increment_discount_usage()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE discounts SET usage_count = usage_count + 1
    WHERE id = NEW.discount_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER discount_usage_increment_trigger
    AFTER INSERT ON discount_usages
    FOR EACH ROW
    EXECUTE FUNCTION increment_discount_usage();
