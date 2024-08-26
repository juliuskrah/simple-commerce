CREATE TABLE IF NOT EXISTS media (
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by VARCHAR(250),
    updated_by VARCHAR(250),
    id UUID NOT NULL,
    product_id UUID,
    url VARCHAR(512) NOT NULL,
    content_type VARCHAR(126),
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS media
    ADD CONSTRAINT media_product_id_fk
    FOREIGN KEY (product_id)
    REFERENCES products;

ALTER TABLE IF EXISTS media
    ADD CONSTRAINT media_product_id_url_unique
    UNIQUE (product_id, url)
