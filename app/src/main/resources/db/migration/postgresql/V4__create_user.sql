CREATE TABLE IF NOT EXISTS users (
    last_login TIMESTAMP(6) WITH TIME ZONE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by VARCHAR(250),
    updated_by VARCHAR(250),
    id UUID NOT NULL,
    email VARCHAR(512) NOT NULL,
    username VARCHAR(250) NOT NULL,
    user_type VARCHAR(50) NOT NULL DEFAULT 'STAFF',
    external_id VARCHAR(128),
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS users
    ADD CONSTRAINT users_username_unique
    UNIQUE (username);

ALTER TABLE IF EXISTS users
    ADD CONSTRAINT users_external_id_unique
    UNIQUE NULLS NOT DISTINCT (external_id);

ALTER TABLE IF EXISTS users
    ADD CONSTRAINT users_email_unique
    UNIQUE (email);

-- Add check constraint for user type
ALTER TABLE IF EXISTS users
    ADD CONSTRAINT user_type_check
        CHECK (user_type IN ('STAFF', 'COLLABORATOR', 'CUSTOMER'))
