CREATE SCHEMA IF NOT EXISTS shopping_cart;

CREATE TABLE IF NOT EXISTS shopping_cart.shopping_carts (
    shopping_cart_id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL
    );

CREATE TABLE IF NOT EXISTS shopping_cart.shopping_cart_products (
    shopping_cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL,
    PRIMARY KEY (shopping_cart_id, product_id)
    );

CREATE INDEX IF NOT EXISTS idx_shopping_carts_username_active
    ON shopping_cart.shopping_carts (username, active);