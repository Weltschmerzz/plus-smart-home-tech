CREATE SCHEMA IF NOT EXISTS warehouse;

CREATE TABLE IF NOT EXISTS warehouse.warehouse_products (
     product_id UUID PRIMARY KEY,
     fragile BOOLEAN NOT NULL,
     width DOUBLE PRECISION NOT NULL,
     height DOUBLE PRECISION NOT NULL,
     depth DOUBLE PRECISION NOT NULL,
     weight DOUBLE PRECISION NOT NULL,
     quantity BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS warehouse.order_bookings (
     order_id UUID PRIMARY KEY,
     delivery_id UUID,
     delivery_weight DOUBLE PRECISION NOT NULL,
     delivery_volume DOUBLE PRECISION NOT NULL,
     fragile BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS warehouse.order_booking_products (
     order_id UUID NOT NULL,
     product_id UUID NOT NULL,
     quantity BIGINT NOT NULL,
     PRIMARY KEY (order_id, product_id),
     CONSTRAINT fk_order_booking_products_order
         FOREIGN KEY (order_id)
         REFERENCES warehouse.order_bookings(order_id)
);
