package ru.yandex.practicum.commerce.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.api.enumtype.QuantityState;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetProductQuantityStateRequest {

    @NotNull
    private UUID productId;

    @NotNull
    private QuantityState quantityState;
}