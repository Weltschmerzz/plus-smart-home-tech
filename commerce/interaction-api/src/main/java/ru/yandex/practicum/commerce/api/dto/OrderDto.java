package ru.yandex.practicum.commerce.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.api.enumtype.OrderState;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private UUID orderId;

    @NotNull
    private UUID shoppingCartId;

    private UUID paymentId;

    private UUID deliveryId;

    private OrderState state;

    @NotNull
    private Map<UUID, Long> products;

    private Double deliveryWeight;

    private Double deliveryVolume;

    private Boolean fragile;

    private BigDecimal totalPrice;

    private BigDecimal productPrice;

    private BigDecimal deliveryPrice;
}