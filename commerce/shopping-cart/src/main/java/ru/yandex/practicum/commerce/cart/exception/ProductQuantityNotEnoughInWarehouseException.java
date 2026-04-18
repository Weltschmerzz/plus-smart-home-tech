package ru.yandex.practicum.commerce.cart.exception;

public class ProductQuantityNotEnoughInWarehouseException extends RuntimeException {

    public ProductQuantityNotEnoughInWarehouseException(String message) {
        super(message);
    }
}