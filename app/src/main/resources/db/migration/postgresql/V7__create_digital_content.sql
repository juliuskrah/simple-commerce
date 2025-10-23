CREATE TABLE IF NOT EXISTS digital_content (
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by VARCHAR(250),
    updated_by VARCHAR(250),
    id UUID NOT NULL,
    variant_id UUID NOT NULL,
    url VARCHAR(512) NOT NULL,
    content_type VARCHAR(126),
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS digital_content
    ADD CONSTRAINT digital_content_variant_id_fk
    FOREIGN KEY (variant_id)
    REFERENCES product_variants;

ALTER TABLE IF EXISTS digital_content
    ADD CONSTRAINT digital_content_variant_id_url_unique
    UNIQUE (variant_id, url);
