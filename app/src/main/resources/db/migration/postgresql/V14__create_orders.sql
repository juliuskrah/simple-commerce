-- Create orders table
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    -- Financial details
    subtotal_amount DECIMAL(19, 4) NOT NULL,
    subtotal_currency VARCHAR(3) NOT NULL,
    tax_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    tax_currency VARCHAR(3) NOT NULL,
    shipping_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    shipping_currency VARCHAR(3) NOT NULL,
    discount_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    discount_currency VARCHAR(3) NOT NULL,
    total_amount DECIMAL(19, 4) NOT NULL,
    total_currency VARCHAR(3) NOT NULL,

    -- Customer information
    customer_email VARCHAR(255) NOT NULL,
    customer_name VARCHAR(255),

    -- Shipping address
    shipping_first_name VARCHAR(100),
    shipping_last_name VARCHAR(100),
    shipping_company VARCHAR(100),
    shipping_address_line1 VARCHAR(255),
    shipping_address_line2 VARCHAR(255),
    shipping_city VARCHAR(100),
    shipping_state VARCHAR(100),
    shipping_postal_code VARCHAR(20),
    shipping_country VARCHAR(2),
    shipping_phone VARCHAR(20),

    -- Billing address
    billing_first_name VARCHAR(100),
    billing_last_name VARCHAR(100),
    billing_company VARCHAR(100),
    billing_address_line1 VARCHAR(255),
    billing_address_line2 VARCHAR(255),
    billing_city VARCHAR(100),
    billing_state VARCHAR(100),
    billing_postal_code VARCHAR(20),
    billing_country VARCHAR(2),
    billing_phone VARCHAR(20),

    -- Metadata
    notes TEXT,
    customer_notes TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,

    -- Timestamps
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    placed_at TIMESTAMP(6) WITH TIME ZONE,
    confirmed_at TIMESTAMP(6) WITH TIME ZONE,
    fulfilled_at TIMESTAMP(6) WITH TIME ZONE,
    cancelled_at TIMESTAMP(6) WITH TIME ZONE,

    CONSTRAINT check_order_status CHECK (
        status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'FULFILLED', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED')
    )
);

-- Create order_items table
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    variant_id UUID NOT NULL REFERENCES product_variants(id) ON DELETE RESTRICT,

    -- Product snapshot at time of order
    product_title VARCHAR(500) NOT NULL,
    variant_title VARCHAR(500) NOT NULL,
    sku VARCHAR(100) NOT NULL,

    -- Pricing
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price_amount DECIMAL(19, 4) NOT NULL,
    unit_price_currency VARCHAR(3) NOT NULL,
    total_price_amount DECIMAL(19, 4) NOT NULL,
    total_price_currency VARCHAR(3) NOT NULL,
    tax_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    tax_currency VARCHAR(3) NOT NULL,
    discount_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    discount_currency VARCHAR(3) NOT NULL,

    -- Fulfillment
    fulfillment_status VARCHAR(50) NOT NULL DEFAULT 'UNFULFILLED',

    -- Timestamps
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_fulfillment_status CHECK (
        fulfillment_status IN ('UNFULFILLED', 'PARTIAL', 'FULFILLED', 'RETURNED', 'CANCELLED')
    )
);

-- Create indexes for orders
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);
CREATE INDEX idx_orders_customer_email ON orders(customer_email);

-- Create indexes for order_items
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_variant_id ON order_items(variant_id);
CREATE INDEX idx_order_items_sku ON order_items(sku);

-- Create trigger for orders updated_at
CREATE TRIGGER order_updated_at_trigger
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_updated_at();

-- Create trigger for order_items updated_at
CREATE TRIGGER order_item_updated_at_trigger
    BEFORE UPDATE ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_updated_at();

-- Create function to update order updated_at when items change
CREATE OR REPLACE FUNCTION update_order_on_item_change()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE orders SET updated_at = CURRENT_TIMESTAMP
    WHERE id = COALESCE(NEW.order_id, OLD.order_id);
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER order_item_change_trigger
    AFTER INSERT OR UPDATE OR DELETE ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION update_order_on_item_change();

-- Create sequence for order numbers
CREATE SEQUENCE order_number_seq START 1000;