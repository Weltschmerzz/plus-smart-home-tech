package ru.yandex.practicum.commerce.cart.exception;

public class WarehouseServiceUnavailableException extends RuntimeException {

    public WarehouseServiceUnavailableException(String message) {
        super(message);
    }
}