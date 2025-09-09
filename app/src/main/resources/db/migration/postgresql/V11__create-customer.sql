-- Create customer table for storefront users
CREATE TABLE IF NOT EXISTS customer (
    id UUID NOT NULL,
    username VARCHAR(250) NOT NULL,
    email VARCHAR(512),
    external_id VARCHAR(128),
    actor_type VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    customer_group VARCHAR(50) NOT NULL DEFAULT 'B2C',
    last_login TIMESTAMP(6) WITH TIME ZONE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by VARCHAR(250),
    updated_by VARCHAR(250),
    PRIMARY KEY (id)
);

-- Add unique constraints
ALTER TABLE customer
    ADD CONSTRAINT customer_username_unique
    UNIQUE (username);

ALTER TABLE customer
    ADD CONSTRAINT customer_email_unique
    UNIQUE (email);

ALTER TABLE customer
    ADD CONSTRAINT customer_external_id_unique
    UNIQUE (external_id);

-- Add check constraint for customer group
ALTER TABLE customer
    ADD CONSTRAINT customer_group_check
    CHECK (customer_group IN ('B2C', 'B2B', 'VIP', 'WHOLESALE'));

-- Add check constraint for actor type
ALTER TABLE customer
    ADD CONSTRAINT customer_actor_type_check
    CHECK (actor_type = 'CUSTOMER');