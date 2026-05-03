package ru.yandex.practicum.commerce.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.api.enumtype.DeliveryState;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDto {

    private UUID deliveryId;

    private UUID orderId;

    private AddressDto fromAddress;

    private AddressDto toAddress;

    private DeliveryState deliveryState;

    private Double deliveryWeight;

    private Double deliveryVolume;

    private Boolean fragile;
}