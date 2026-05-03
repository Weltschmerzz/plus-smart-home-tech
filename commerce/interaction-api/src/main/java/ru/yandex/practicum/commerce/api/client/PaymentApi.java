package ru.yandex.practicum.commerce.api.client;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.api.dto.OrderDto;
import ru.yandex.practicum.commerce.api.dto.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

@RequestMapping("/api/v1/payment")
public interface PaymentApi {

    @PostMapping("/productCost")
    BigDecimal productCost(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/totalCost")
    BigDecimal getTotalCost(@Valid @RequestBody OrderDto orderDto);

    @PostMapping
    PaymentDto payment(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/success")
    void paymentSuccess(@RequestBody UUID paymentId);

    @PostMapping("/failed")
    void paymentFailed(@RequestBody UUID paymentId);
}