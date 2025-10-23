CREATE TABLE IF NOT EXISTS product_variants (
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by VARCHAR(250),
    updated_by VARCHAR(250),
    id UUID NOT NULL,
    product_id UUID NOT NULL,
    sku VARCHAR(128) NOT NULL,
    title VARCHAR(500),
    price_amount NUMERIC(19,4),
    price_currency VARCHAR(3),
    system_generated BOOLEAN NOT NULL DEFAULT true,
    PRIMARY KEY (id)
);

ALTER TABLE IF EXISTS product_variants
    ADD CONSTRAINT product_variants_product_id_fk
    FOREIGN KEY (product_id)
    REFERENCES products;

ALTER TABLE IF EXISTS product_variants
    ADD CONSTRAINT product_variants_sku_unique
    UNIQUE (sku);
