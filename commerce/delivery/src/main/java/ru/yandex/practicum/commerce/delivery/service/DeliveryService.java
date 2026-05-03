package ru.yandex.practicum.commerce.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.dto.AddressDto;
import ru.yandex.practicum.commerce.api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.api.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.api.enumtype.DeliveryState;
import ru.yandex.practicum.commerce.delivery.client.OrderClient;
import ru.yandex.practicum.commerce.delivery.client.WarehouseClient;
import ru.yandex.practicum.commerce.delivery.exception.DeliveryNotFoundException;
import ru.yandex.practicum.commerce.delivery.model.Delivery;
import ru.yandex.practicum.commerce.delivery.model.DeliveryAddress;
import ru.yandex.practicum.commerce.delivery.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {

    private static final BigDecimal BASE_COST = BigDecimal.valueOf(5.0);
    private static final BigDecimal ADDRESS_2_MULTIPLIER = BigDecimal.valueOf(2.0);
    private static final BigDecimal DEFAULT_WAREHOUSE_MULTIPLIER = BigDecimal.valueOf(1.0);
    private static final BigDecimal FRAGILE_RATE = BigDecimal.valueOf(0.2);
    private static final BigDecimal WEIGHT_RATE = BigDecimal.valueOf(0.3);
    private static final BigDecimal VOLUME_RATE = BigDecimal.valueOf(0.2);
    private static final BigDecimal DIFFERENT_STREET_RATE = BigDecimal.valueOf(0.2);
    private static final int MONEY_SCALE = 2;

    private static final String ADDRESS_2 = "ADDRESS_2";

    private final DeliveryRepository deliveryRepository;
    private final WarehouseClient warehouseClient;
    private final OrderClient orderClient;

    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = Delivery.builder()
                .deliveryId(UUID.randomUUID())
                .orderId(deliveryDto.getOrderId())
                .fromAddress(toAddressEntity(deliveryDto.getFromAddress()))
                .toAddress(toAddressEntity(deliveryDto.getToAddress()))
                .deliveryWeight(nullToZero(deliveryDto.getDeliveryWeight()))
                .deliveryVolume(nullToZero(deliveryDto.getDeliveryVolume()))
                .fragile(Boolean.TRUE.equals(deliveryDto.getFragile()))
                .deliveryState(DeliveryState.CREATED)
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);

        return toDto(savedDelivery);
    }

    public BigDecimal deliveryCost(DeliveryDto deliveryDto) {
        BigDecimal result = calculateWarehousePart(deliveryDto.getFromAddress());

        if (Boolean.TRUE.equals(deliveryDto.getFragile())) {
            result = result.add(result.multiply(FRAGILE_RATE));
        }

        result = result.add(BigDecimal.valueOf(nullToZero(deliveryDto.getDeliveryWeight())).multiply(WEIGHT_RATE));
        result = result.add(BigDecimal.valueOf(nullToZero(deliveryDto.getDeliveryVolume())).multiply(VOLUME_RATE));

        if (!isSameStreet(deliveryDto.getFromAddress(), deliveryDto.getToAddress())) {
            result = result.add(result.multiply(DIFFERENT_STREET_RATE));
        }

        return scale(result);
    }

    @Transactional
    public void deliveryPicked(UUID deliveryId) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);

        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);

        warehouseClient.shippedToDelivery(
                ShippedToDeliveryRequest.builder()
                        .orderId(delivery.getOrderId())
                        .deliveryId(delivery.getDeliveryId())
                        .build()
        );
    }

    @Transactional
    public void deliverySuccessful(UUID deliveryId) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);

        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);

        orderClient.delivery(delivery.getOrderId());
    }

    @Transactional
    public void deliveryFailed(UUID deliveryId) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);

        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);

        orderClient.deliveryFailed(delivery.getOrderId());
    }

    private Delivery getDeliveryOrThrow(UUID deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));
    }

    private BigDecimal calculateWarehousePart(AddressDto fromAddress) {
        BigDecimal multiplier = containsAddress2(fromAddress)
                ? ADDRESS_2_MULTIPLIER
                : DEFAULT_WAREHOUSE_MULTIPLIER;

        return BASE_COST.multiply(multiplier).add(BASE_COST);
    }

    private boolean containsAddress2(AddressDto address) {
        if (address == null) {
            return false;
        }

        String fullAddress = String.join(" ",
                nullToEmpty(address.getCountry()),
                nullToEmpty(address.getCity()),
                nullToEmpty(address.getStreet()),
                nullToEmpty(address.getHouse()),
                nullToEmpty(address.getFlat())
        );

        return fullAddress.contains(ADDRESS_2);
    }

    private boolean isSameStreet(AddressDto fromAddress, AddressDto toAddress) {
        String fromStreet = fromAddress == null ? null : fromAddress.getStreet();
        String toStreet = toAddress == null ? null : toAddress.getStreet();

        return Objects.equals(fromStreet, toStreet);
    }

    private DeliveryAddress toAddressEntity(AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }

        return DeliveryAddress.builder()
                .country(addressDto.getCountry())
                .city(addressDto.getCity())
                .street(addressDto.getStreet())
                .house(addressDto.getHouse())
                .flat(addressDto.getFlat())
                .build();
    }

    private AddressDto toAddressDto(DeliveryAddress address) {
        if (address == null) {
            return null;
        }

        return AddressDto.builder()
                .country(address.getCountry())
                .city(address.getCity())
                .street(address.getStreet())
                .house(address.getHouse())
                .flat(address.getFlat())
                .build();
    }

    private DeliveryDto toDto(Delivery delivery) {
        return DeliveryDto.builder()
                .deliveryId(delivery.getDeliveryId())
                .orderId(delivery.getOrderId())
                .fromAddress(toAddressDto(delivery.getFromAddress()))
                .toAddress(toAddressDto(delivery.getToAddress()))
                .deliveryWeight(delivery.getDeliveryWeight())
                .deliveryVolume(delivery.getDeliveryVolume())
                .fragile(delivery.getFragile())
                .deliveryState(delivery.getDeliveryState())
                .build();
    }

    private Double nullToZero(Double value) {
        return value == null ? 0.0 : value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
