package ru.yandex.practicum.commerce.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DimensionDto {

    @NotNull
    @Positive
    private Double width;

    @NotNull
    @Positive
    private Double height;

    @NotNull
    @Positive
    private Double depth;
}