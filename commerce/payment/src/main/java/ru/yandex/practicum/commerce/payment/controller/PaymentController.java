package ru.yandex.practicum.commerce.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.client.PaymentApi;
import ru.yandex.practicum.commerce.api.dto.OrderDto;
import ru.yandex.practicum.commerce.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;

    @Override
    public BigDecimal productCost(@Valid OrderDto orderDto) {
        return paymentService.productCost(orderDto);
    }

    @Override
    public BigDecimal getTotalCost(@Valid OrderDto orderDto) {
        return paymentService.getTotalCost(orderDto);
    }

    @Override
    public PaymentDto payment(@Valid OrderDto orderDto) {
        return paymentService.payment(orderDto);
    }

    @Override
    public void paymentSuccess(UUID paymentId) {
        paymentService.paymentSuccess(paymentId);
    }

    @Override
    public void paymentFailed(UUID paymentId) {
        paymentService.paymentFailed(paymentId);
    }
}