package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.UUID;

public class OrderBookingNotFoundException extends RuntimeException {

    public OrderBookingNotFoundException(UUID orderId) {
        super("Сборка заказа с id=" + orderId + " не найдена на складе");
    }
}