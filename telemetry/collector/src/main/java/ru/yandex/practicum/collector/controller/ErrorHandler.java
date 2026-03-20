package ru.yandex.practicum.collector.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Ошибка валидации входных данных");
        body.put("message", errors);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Некорректный JSON");
        body.put("message", e.getMostSpecificCause() != null
                ? e.getMostSpecificCause().getMessage()
                : e.getMessage());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Некорректные данные");
        body.put("message", e.getMessage());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Внутренняя ошибка сервера");
        body.put("message", e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}