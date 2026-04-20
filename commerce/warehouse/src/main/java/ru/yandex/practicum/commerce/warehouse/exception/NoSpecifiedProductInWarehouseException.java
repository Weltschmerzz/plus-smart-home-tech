package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.UUID;

public class NoSpecifiedProductInWarehouseException extends RuntimeException {

    public NoSpecifiedProductInWarehouseException(UUID productId) {
        super("Товар с id=" + productId + " не найден на складе");
    }
}