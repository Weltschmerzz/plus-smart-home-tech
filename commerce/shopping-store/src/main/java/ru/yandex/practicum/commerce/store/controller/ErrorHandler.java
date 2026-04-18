package ru.yandex.practicum.commerce.store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.store.exception.ProductNotFoundException;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleProductNotFound(ProductNotFoundException e) {
        return Map.of(
                "error", "PRODUCT_NOT_FOUND",
                "message", e.getMessage()
        );
    }
}