CREATE TABLE IF NOT EXISTS products (
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    id UUID NOT NULL,
    description TEXT,
    slug VARCHAR(503) NOT NULL UNIQUE,
    title VARCHAR(500),
    PRIMARY KEY (id)
);

CREATE TABLE product_tag (
    product_id uuid NOT NULL,
    tags VARCHAR(500)
);

ALTER TABLE IF EXISTS product_tag
    ADD CONSTRAINT products_tag_product_id_fk
    FOREIGN KEY (product_id)
    REFERENCES products
