package ru.yandex.practicum.commerce.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.api.enumtype.PaymentState;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    private UUID paymentId;

    private UUID orderId;

    private BigDecimal productTotal;

    private BigDecimal deliveryTotal;

    private BigDecimal totalPayment;

    private PaymentState paymentState;
}