-- Insert seed data for discount codes

-- Percentage discounts
INSERT INTO discounts (id, code, name, description, discount_type, value_percentage, value_amount,
                       applies_to, minimum_order_amount, minimum_quantity, usage_limit, usage_count,
                       per_customer_limit, starts_at, ends_at, active, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'WELCOME10', 'Welcome 10% Off',
     'New customer discount - 10% off your first order', 'PERCENTAGE',
     10.00, NULL, 'ORDER', 25.00, NULL, 1000, 0, 1, NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), 'SAVE20', 'Save 20% Today',
     '20% off your entire order', 'PERCENTAGE',
     20.00, NULL, 'ORDER', 50.00, NULL, 500, 0, NULL, NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), 'VIP25', 'VIP 25% Discount',
     'VIP customer exclusive - 25% off', 'PERCENTAGE',
     25.00, NULL, 'ORDER', 100.00, NULL, 100, 0, 5, NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Fixed amount discounts
INSERT INTO discounts (id, code, name, description, discount_type, value_percentage, value_amount,
                       applies_to, minimum_order_amount, minimum_quantity, usage_limit, usage_count,
                       per_customer_limit, starts_at, ends_at, active, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'SAVE5', 'Save $5',
     'Get $5 off orders over $25', 'FIXED_AMOUNT',
     NULL, 5.00, 'ORDER', 25.00, NULL, 2000, 0, NULL, NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), 'TEN4ME', '$10 Off',
     'Take $10 off your order of $50 or more', 'FIXED_AMOUNT',
     NULL, 10.00, 'ORDER', 50.00, NULL, 1000, 0, NULL, NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), 'BIGDEAL', '$25 Off Big Orders',
     'Save $25 on orders over $150', 'FIXED_AMOUNT',
     NULL, 25.00, 'ORDER', 150.00, NULL, 200, 0, NULL, NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Free shipping discount
INSERT INTO discounts (id, code, name, description, discount_type, value_percentage, value_amount,
                       applies_to, minimum_order_amount, minimum_quantity, usage_limit, usage_count,
                       per_customer_limit, starts_at, ends_at, active, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'FREESHIP', 'Free Shipping',
     'Free standard shipping on any order', 'FREE_SHIPPING',
     NULL, NULL, 'SHIPPING', NULL, NULL, NULL, 0, NULL, NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), 'SHIPFREE50', 'Free Shipping Over $50',
     'Free shipping on orders over $50', 'FREE_SHIPPING',
     NULL, NULL, 'SHIPPING', 50.00, NULL, NULL, 0, NULL, NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seasonal/time-limited discounts
INSERT INTO discounts (id, code, name, description, discount_type, value_percentage, value_amount,
                       applies_to, minimum_order_amount, minimum_quantity, usage_limit, usage_count,
                       per_customer_limit, starts_at, ends_at, active, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'SUMMER2025', 'Summer Sale 2025',
     'Summer special - 15% off everything', 'PERCENTAGE',
     15.00, NULL, 'ORDER', NULL, NULL, 5000, 0, NULL,
     '2025-06-01 00:00:00+00'::timestamptz, '2025-08-31 23:59:59+00'::timestamptz,
     true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), 'BLACKFRIDAY', 'Black Friday Sale',
     'Black Friday - 30% off everything', 'PERCENTAGE',
     30.00, NULL, 'ORDER', NULL, NULL, 10000, 0, 3,
     '2025-11-28 00:00:00+00'::timestamptz, '2025-11-30 23:59:59+00'::timestamptz,
     true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Expired discount (for testing)
INSERT INTO discounts (id, code, name, description, discount_type, value_percentage, value_amount,
                       applies_to, minimum_order_amount, minimum_quantity, usage_limit, usage_count,
                       per_customer_limit, starts_at, ends_at, active, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'EXPIRED', 'Expired Discount',
     'This discount has expired', 'PERCENTAGE',
     50.00, NULL, 'ORDER', NULL, NULL, 100, 0, NULL,
     '2024-01-01 00:00:00+00'::timestamptz, '2024-12-31 23:59:59+00'::timestamptz,
     true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Nearly exhausted discount (for testing usage limits)
INSERT INTO discounts (id, code, name, description, discount_type, value_percentage, value_amount,
                       applies_to, minimum_order_amount, minimum_quantity, usage_limit, usage_count,
                       per_customer_limit, starts_at, ends_at, active, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'ALMOSTGONE', 'Almost Gone',
     'Limited quantity - only 5 uses left', 'PERCENTAGE',
     15.00, NULL, 'ORDER', NULL, NULL, 10, 5, NULL,
     NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Inactive discount (for testing active status)
INSERT INTO discounts (id, code, name, description, discount_type, value_percentage, value_amount,
                       applies_to, minimum_order_amount, minimum_quantity, usage_limit, usage_count,
                       per_customer_limit, starts_at, ends_at, active, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'INACTIVE', 'Inactive Discount',
     'This discount is not currently active', 'PERCENTAGE',
     20.00, NULL, 'ORDER', NULL, NULL, 100, 0, NULL,
     NULL, NULL, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
