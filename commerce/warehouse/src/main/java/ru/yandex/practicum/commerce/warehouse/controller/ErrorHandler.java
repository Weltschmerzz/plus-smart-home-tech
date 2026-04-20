package ru.yandex.practicum.commerce.warehouse.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.warehouse.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.warehouse.exception.SpecifiedProductAlreadyInWarehouseException;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleSpecifiedProductAlreadyInWarehouse(
            SpecifiedProductAlreadyInWarehouseException e
    ) {
        return Map.of(
                "error", "SPECIFIED_PRODUCT_ALREADY_IN_WAREHOUSE",
                "message", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleNoSpecifiedProductInWarehouse(
            NoSpecifiedProductInWarehouseException e
    ) {
        return Map.of(
                "error", "NO_SPECIFIED_PRODUCT_IN_WAREHOUSE",
                "message", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleLowQuantity(
            ProductInShoppingCartLowQuantityInWarehouse e
    ) {
        return Map.of(
                "error", "PRODUCT_IN_SHOPPING_CART_LOW_QUANTITY_IN_WAREHOUSE",
                "message", e.getMessage(),
                "missingProducts", e.getMissingProducts()
        );
    }
}