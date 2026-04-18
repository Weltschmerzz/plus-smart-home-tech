package ru.yandex.practicum.commerce.cart.exception;

import java.util.UUID;

public class ProductNotFoundInShoppingCartException extends RuntimeException {

    public ProductNotFoundInShoppingCartException(UUID productId) {
        super("Товар с id=" + productId + " отсутствует в корзине");
    }
}