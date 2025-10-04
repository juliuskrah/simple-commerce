-- Create bot table for automation and external applications
CREATE TABLE IF NOT EXISTS bots (
    id UUID NOT NULL,
    username VARCHAR(250) NOT NULL,
    email VARCHAR(512),
    external_id VARCHAR(128),
    api_key VARCHAR(512),
    app_id VARCHAR(100),
    permissions TEXT, -- JSON array of permissions
    last_login TIMESTAMP(6) WITH TIME ZONE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by VARCHAR(250),
    updated_by VARCHAR(250),
    PRIMARY KEY (id)
);

-- Add unique constraints
ALTER TABLE bots
    ADD CONSTRAINT bot_username_unique
    UNIQUE (username);

ALTER TABLE bots
    ADD CONSTRAINT bot_email_unique
    UNIQUE (email);

ALTER TABLE bots
    ADD CONSTRAINT bot_external_id_unique
    UNIQUE (external_id);

ALTER TABLE bots
    ADD CONSTRAINT bot_api_key_unique
    UNIQUE (api_key);
