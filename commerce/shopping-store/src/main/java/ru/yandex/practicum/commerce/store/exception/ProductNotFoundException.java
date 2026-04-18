package ru.yandex.practicum.commerce.store.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(UUID productId) {
        super("Товар с id=" + productId + " не найден");
    }
}