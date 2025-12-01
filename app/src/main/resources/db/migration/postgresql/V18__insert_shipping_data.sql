-- Insert seed data for shipping zones and methods

-- Create shipping zone for United States
INSERT INTO shipping_zones (id, name, description, active, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000001'::uuid, 'United States', 'Domestic shipping within the US', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000002'::uuid, 'International', 'International shipping to Canada and Mexico', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000003'::uuid, 'Europe', 'European Union countries', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Add locations for US shipping zone (all 50 states)
INSERT INTO shipping_zone_locations (id, zone_id, country_code, state_code, created_at)
SELECT
    gen_random_uuid(),
    '00000000-0000-0000-0000-000000000001'::uuid,
    'US',
    state_code,
    CURRENT_TIMESTAMP
FROM (VALUES
    ('AL'), ('AK'), ('AZ'), ('AR'), ('CA'), ('CO'), ('CT'), ('DE'), ('FL'), ('GA'),
    ('HI'), ('ID'), ('IL'), ('IN'), ('IA'), ('KS'), ('KY'), ('LA'), ('ME'), ('MD'),
    ('MA'), ('MI'), ('MN'), ('MS'), ('MO'), ('MT'), ('NE'), ('NV'), ('NH'), ('NJ'),
    ('NM'), ('NY'), ('NC'), ('ND'), ('OH'), ('OK'), ('OR'), ('PA'), ('RI'), ('SC'),
    ('SD'), ('TN'), ('TX'), ('UT'), ('VT'), ('VA'), ('WA'), ('WV'), ('WI'), ('WY')
) AS states(state_code);

-- Add locations for International zone
INSERT INTO shipping_zone_locations (id, zone_id, country_code, state_code, created_at)
VALUES
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000002'::uuid, 'CA', NULL, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000002'::uuid, 'MX', NULL, CURRENT_TIMESTAMP);

-- Add locations for Europe zone
INSERT INTO shipping_zone_locations (id, zone_id, country_code, state_code, created_at)
SELECT
    gen_random_uuid(),
    '00000000-0000-0000-0000-000000000003'::uuid,
    country_code,
    NULL,
    CURRENT_TIMESTAMP
FROM (VALUES
    ('GB'), ('DE'), ('FR'), ('IT'), ('ES'), ('NL'), ('BE'), ('AT'), ('SE'), ('PL')
) AS countries(country_code);

-- Add shipping methods for US zone
INSERT INTO shipping_methods (id, zone_id, name, description, code, carrier, price_amount, price_currency,
                               min_delivery_days, max_delivery_days, min_order_amount, max_order_amount,
                               active, created_at, updated_at)
VALUES
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001'::uuid,
     'Standard Shipping', 'USPS Ground Shipping - 5-7 business days', 'US_STANDARD', 'USPS',
     5.99, 'USD', 5, 7, NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001'::uuid,
     'Express Shipping', 'FedEx Express - 2-3 business days', 'US_EXPRESS', 'FedEx',
     14.99, 'USD', 2, 3, NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001'::uuid,
     'Next Day Air', 'UPS Next Day Air - 1 business day', 'US_NEXT_DAY', 'UPS',
     29.99, 'USD', 1, 1, NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001'::uuid,
     'Free Shipping', 'Free standard shipping on orders over $50', 'US_FREE', 'USPS',
     0.00, 'USD', 5, 7, 50.00, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Add shipping methods for International zone
INSERT INTO shipping_methods (id, zone_id, name, description, code, carrier, price_amount, price_currency,
                               min_delivery_days, max_delivery_days, min_order_amount, max_order_amount,
                               active, created_at, updated_at)
VALUES
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000002'::uuid,
     'International Standard', 'Standard international shipping - 7-14 business days', 'INTL_STANDARD', 'USPS',
     19.99, 'USD', 7, 14, NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), '00000000-0000-0000-0000-000000000002'::uuid,
     'International Express', 'Express international shipping - 3-5 business days', 'INTL_EXPRESS', 'FedEx',
     39.99, 'USD', 3, 5, NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Add shipping methods for Europe zone
INSERT INTO shipping_methods (id, zone_id, name, description, code, carrier, price_amount, price_currency,
                               min_delivery_days, max_delivery_days, min_order_amount, max_order_amount,
                               active, created_at, updated_at)
VALUES
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000003'::uuid,
     'EU Standard Shipping', 'Standard European shipping - 5-10 business days', 'EU_STANDARD', 'DHL',
     12.99, 'EUR', 5, 10, NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), '00000000-0000-0000-0000-000000000003'::uuid,
     'EU Express Shipping', 'Express European shipping - 2-4 business days', 'EU_EXPRESS', 'DHL',
     24.99, 'EUR', 2, 4, NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
