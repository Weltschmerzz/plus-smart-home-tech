package ru.yandex.practicum.commerce.cart.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.cart.exception.ProductNotFoundInShoppingCartException;
import ru.yandex.practicum.commerce.cart.exception.ProductQuantityNotEnoughInWarehouseException;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleProductNotFoundInCart(
            ProductNotFoundInShoppingCartException e
    ) {
        return Map.of(
                "error", "PRODUCT_NOT_FOUND_IN_SHOPPING_CART",
                "message", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleWarehouseLowQuantity(
            ProductQuantityNotEnoughInWarehouseException e
    ) {
        return Map.of(
                "error", "PRODUCT_QUANTITY_NOT_ENOUGH_IN_WAREHOUSE",
                "message", e.getMessage()
        );
    }
}