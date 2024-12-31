ALTER TABLE IF EXISTS products
    ADD COLUMN category_id UUID DEFAULT '6ef9c5ce-0430-468e-8adb-523fc05c4a11',
    ADD CONSTRAINT products_category_id_fk
    FOREIGN KEY (category_id)
    REFERENCES categories ON
DELETE
SET DEFAULT;
