CREATE SCHEMA IF NOT EXISTS shopping_store;

CREATE TABLE IF NOT EXISTS shopping_store.products (
    product_id UUID PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    image_src TEXT,
    quantity_state VARCHAR(50) NOT NULL,
    product_state VARCHAR(50) NOT NULL,
    product_category VARCHAR(50),
    price NUMERIC(19, 2) NOT NULL
    );