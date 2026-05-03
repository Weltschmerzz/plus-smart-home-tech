package ru.yandex.practicum.commerce.delivery.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddress {

    private String country;

    private String city;

    private String street;

    private String house;

    private String flat;
}
