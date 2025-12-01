-- Create shipping_zones table
CREATE TABLE shipping_zones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create shipping_zone_locations table (countries/regions in a zone)
CREATE TABLE shipping_zone_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    zone_id UUID NOT NULL REFERENCES shipping_zones(id) ON DELETE CASCADE,
    country_code VARCHAR(2) NOT NULL,
    state_code VARCHAR(100),
    postal_code_pattern VARCHAR(100),
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_zone_location UNIQUE (zone_id, country_code, state_code)
);

-- Create shipping_methods table
CREATE TABLE shipping_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    zone_id UUID NOT NULL REFERENCES shipping_zones(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    code VARCHAR(100) NOT NULL,
    carrier VARCHAR(100),

    -- Pricing
    price_amount DECIMAL(19, 4) NOT NULL,
    price_currency VARCHAR(3) NOT NULL,

    -- Conditions
    min_order_amount DECIMAL(19, 4),
    max_order_amount DECIMAL(19, 4),
    min_weight_grams INT,
    max_weight_grams INT,

    -- Delivery estimates
    min_delivery_days INT,
    max_delivery_days INT,

    -- Status
    active BOOLEAN NOT NULL DEFAULT true,

    -- Timestamps
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_zone_method_code UNIQUE (zone_id, code)
);

-- Create shipments table (fulfillment tracking)
CREATE TABLE shipments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    shipping_method_id UUID REFERENCES shipping_methods(id) ON DELETE SET NULL,

    -- Tracking information
    tracking_number VARCHAR(255),
    tracking_url TEXT,
    carrier VARCHAR(100),
    service VARCHAR(100),

    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    -- Shipping details
    shipped_at TIMESTAMP(6) WITH TIME ZONE,
    estimated_delivery_at TIMESTAMP(6) WITH TIME ZONE,
    delivered_at TIMESTAMP(6) WITH TIME ZONE,

    -- Address (denormalized from order for history)
    recipient_name VARCHAR(255),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(2),
    phone VARCHAR(20),

    -- Metadata
    notes TEXT,

    -- Timestamps
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_shipment_status CHECK (
        status IN ('PENDING', 'LABEL_CREATED', 'SHIPPED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED', 'CANCELLED')
    )
);

-- Create shipment_items table (which order items are in which shipment)
CREATE TABLE shipment_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id UUID NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    order_item_id UUID NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    quantity INT NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_shipment_order_item UNIQUE (shipment_id, order_item_id)
);

-- Create indexes
CREATE INDEX idx_shipping_zone_locations_zone_id ON shipping_zone_locations(zone_id);
CREATE INDEX idx_shipping_zone_locations_country ON shipping_zone_locations(country_code);
CREATE INDEX idx_shipping_methods_zone_id ON shipping_methods(zone_id);
CREATE INDEX idx_shipping_methods_active ON shipping_methods(active) WHERE active = true;
CREATE INDEX idx_shipments_order_id ON shipments(order_id);
CREATE INDEX idx_shipments_status ON shipments(status);
CREATE INDEX idx_shipments_tracking_number ON shipments(tracking_number);
CREATE INDEX idx_shipment_items_shipment_id ON shipment_items(shipment_id);
CREATE INDEX idx_shipment_items_order_item_id ON shipment_items(order_item_id);

-- Create triggers for updated_at
CREATE TRIGGER shipping_zone_updated_at_trigger
    BEFORE UPDATE ON shipping_zones
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_updated_at();

CREATE TRIGGER shipping_method_updated_at_trigger
    BEFORE UPDATE ON shipping_methods
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_updated_at();

CREATE TRIGGER shipment_updated_at_trigger
    BEFORE UPDATE ON shipments
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_updated_at();
