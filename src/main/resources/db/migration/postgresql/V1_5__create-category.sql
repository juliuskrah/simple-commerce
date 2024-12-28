CREATE EXTENSION IF NOT EXISTS ltree;

CREATE TABLE IF NOT EXISTS categories (
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by VARCHAR(250),
    updated_by VARCHAR(250),
    id UUID NOT NULL,
    description TEXT,
    slug VARCHAR(503) NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    path LTREE,
    PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS categories_path_gist_idx ON categories USING GIST (path);

ALTER TABLE IF EXISTS products
    ADD COLUMN category_id UUID,
    ADD CONSTRAINT products_category_id_fk
    FOREIGN KEY (category_id)
    REFERENCES categories;
