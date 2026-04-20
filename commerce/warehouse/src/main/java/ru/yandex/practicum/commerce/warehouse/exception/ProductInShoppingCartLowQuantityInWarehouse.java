package ru.yandex.practicum.commerce.warehouse.exception;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class ProductInShoppingCartLowQuantityInWarehouse extends RuntimeException {

    private final Map<UUID, Long> missingProducts;

    public ProductInShoppingCartLowQuantityInWarehouse(Map<UUID, Long> missingProducts) {
        super("Недостаточно товара на складе для товаров из корзины");
        this.missingProducts = missingProducts;
    }
}