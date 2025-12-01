-- Create payments table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,

    -- Payment identification
    payment_method VARCHAR(50) NOT NULL,
    payment_provider VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    provider_payment_id VARCHAR(255),

    -- Amount
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,

    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    -- Payment details
    card_last_four VARCHAR(4),
    card_brand VARCHAR(50),
    card_exp_month INT,
    card_exp_year INT,

    -- Metadata
    metadata JSONB,
    provider_response JSONB,
    error_message TEXT,
    error_code VARCHAR(100),

    -- Risk assessment
    risk_level VARCHAR(20),
    fraud_score DECIMAL(5, 2),

    -- Timestamps
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    authorized_at TIMESTAMP(6) WITH TIME ZONE,
    captured_at TIMESTAMP(6) WITH TIME ZONE,
    failed_at TIMESTAMP(6) WITH TIME ZONE,
    refunded_at TIMESTAMP(6) WITH TIME ZONE,

    CONSTRAINT check_payment_status CHECK (
        status IN ('PENDING', 'AUTHORIZED', 'CAPTURED', 'PARTIALLY_REFUNDED', 'REFUNDED', 'FAILED', 'CANCELLED')
    ),
    CONSTRAINT check_payment_method CHECK (
        payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL', 'STRIPE', 'BANK_TRANSFER', 'CASH_ON_DELIVERY', 'CRYPTO')
    ),
    CONSTRAINT check_risk_level CHECK (
        risk_level IS NULL OR risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')
    )
);

-- Create refunds table
CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,

    -- Refund details
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    reason VARCHAR(255),
    reason_code VARCHAR(50),
    notes TEXT,

    -- Provider details
    provider_refund_id VARCHAR(255),
    provider_response JSONB,

    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    -- Timestamps
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP(6) WITH TIME ZONE,
    failed_at TIMESTAMP(6) WITH TIME ZONE,

    CONSTRAINT check_refund_status CHECK (
        status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')
    )
);

-- Create payment_methods table (saved customer payment methods)
CREATE TABLE payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- Payment method details
    type VARCHAR(50) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_customer_id VARCHAR(255),
    provider_payment_method_id VARCHAR(255),

    -- Card details (if applicable)
    card_last_four VARCHAR(4),
    card_brand VARCHAR(50),
    card_exp_month INT,
    card_exp_year INT,

    -- Billing address
    billing_first_name VARCHAR(100),
    billing_last_name VARCHAR(100),
    billing_address_line1 VARCHAR(255),
    billing_address_line2 VARCHAR(255),
    billing_city VARCHAR(100),
    billing_state VARCHAR(100),
    billing_postal_code VARCHAR(20),
    billing_country VARCHAR(2),

    -- Flags
    is_default BOOLEAN NOT NULL DEFAULT false,
    is_verified BOOLEAN NOT NULL DEFAULT false,

    -- Metadata
    metadata JSONB,

    -- Timestamps
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP(6) WITH TIME ZONE,

    CONSTRAINT check_payment_method_type CHECK (
        type IN ('CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL', 'BANK_ACCOUNT', 'CRYPTO_WALLET')
    )
);

-- Create indexes
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);
CREATE INDEX idx_payments_provider_payment_id ON payments(provider_payment_id);
CREATE INDEX idx_payments_created_at ON payments(created_at DESC);

CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
CREATE INDEX idx_refunds_order_id ON refunds(order_id);
CREATE INDEX idx_refunds_status ON refunds(status);

CREATE INDEX idx_payment_methods_customer_id ON payment_methods(customer_id);
CREATE INDEX idx_payment_methods_default ON payment_methods(is_default) WHERE is_default = true;
CREATE INDEX idx_payment_methods_provider ON payment_methods(provider, provider_payment_method_id);

-- Create triggers for updated_at
CREATE TRIGGER payment_updated_at_trigger
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_updated_at();

CREATE TRIGGER refund_updated_at_trigger
    BEFORE UPDATE ON refunds
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_updated_at();

CREATE TRIGGER payment_method_updated_at_trigger
    BEFORE UPDATE ON payment_methods
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_updated_at();
