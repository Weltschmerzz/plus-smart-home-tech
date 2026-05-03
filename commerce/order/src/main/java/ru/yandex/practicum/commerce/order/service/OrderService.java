package ru.yandex.practicum.commerce.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.dto.AddressDto;
import ru.yandex.practicum.commerce.api.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.api.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.api.dto.OrderDto;
import ru.yandex.practicum.commerce.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.api.dto.ProductReturnRequest;
import ru.yandex.practicum.commerce.api.enumtype.OrderState;
import ru.yandex.practicum.commerce.order.client.DeliveryClient;
import ru.yandex.practicum.commerce.order.client.PaymentClient;
import ru.yandex.practicum.commerce.order.client.WarehouseClient;
import ru.yandex.practicum.commerce.order.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.order.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.order.model.OrderAddress;
import ru.yandex.practicum.commerce.order.model.OrderEntity;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final WarehouseClient warehouseClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;

    public List<OrderDto> getClientOrders(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException();
        }

        return orderRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        BookedProductsDto bookedProducts = warehouseClient.checkProductQuantityEnoughForShoppingCart(
                request.getShoppingCart()
        );

        OrderEntity order = OrderEntity.builder()
                .orderId(UUID.randomUUID())
                .shoppingCartId(request.getShoppingCart().getShoppingCartId())
                .products(new HashMap<>(request.getShoppingCart().getProducts()))
                .state(OrderState.NEW)
                .deliveryWeight(bookedProducts.getDeliveryWeight())
                .deliveryVolume(bookedProducts.getDeliveryVolume())
                .fragile(bookedProducts.getFragile())
                .deliveryAddress(toAddressEntity(request.getDeliveryAddress()))
                .build();

        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto assembly(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);

        BookedProductsDto bookedProducts = warehouseClient.assemblyProductForOrderFromShoppingCart(
                AssemblyProductsForOrderRequest.builder()
                        .orderId(order.getOrderId())
                        .products(order.getProducts())
                        .build()
        );

        order.setDeliveryWeight(bookedProducts.getDeliveryWeight());
        order.setDeliveryVolume(bookedProducts.getDeliveryVolume());
        order.setFragile(bookedProducts.getFragile());

        ensureDeliveryPlanned(order);

        order.setState(OrderState.ASSEMBLED);

        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto payment(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);

        if (order.getPaymentId() == null) {
            calculateTotalCost(orderId);
            order = getOrderOrThrow(orderId);

            PaymentDto payment = paymentClient.payment(toDto(order));

            order.setPaymentId(payment.getPaymentId());
            order.setProductPrice(payment.getProductTotal());
            order.setDeliveryPrice(payment.getDeliveryTotal());
            order.setTotalPrice(payment.getTotalPayment());
            order.setState(OrderState.ON_PAYMENT);

            return toDto(orderRepository.save(order));
        }

        order.setState(OrderState.PAID);

        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto delivery(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);

        order.setState(OrderState.DELIVERED);

        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto complete(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);

        order.setState(OrderState.COMPLETED);

        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);

        ensureDeliveryPlanned(order);

        BigDecimal deliveryPrice = deliveryClient.deliveryCost(buildDeliveryDto(order));

        order.setDeliveryPrice(deliveryPrice);

        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);

        if (order.getDeliveryPrice() == null) {
            calculateDeliveryCost(orderId);
            order = getOrderOrThrow(orderId);
        }

        BigDecimal productPrice = paymentClient.productCost(toDto(order));
        order.setProductPrice(productPrice);

        BigDecimal totalPrice = paymentClient.getTotalCost(toDto(order));
        order.setTotalPrice(totalPrice);

        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);

        order.setState(OrderState.PAYMENT_FAILED);

        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);

        order.setState(OrderState.ASSEMBLY_FAILED);

        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        OrderEntity order = getOrderOrThrow(orderId);

        order.setState(OrderState.DELIVERY_FAILED);

        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        OrderEntity order = getOrderOrThrow(request.getOrderId());

        warehouseClient.returnProducts(request);

        order.setState(OrderState.PRODUCT_RETURNED);

        return toDto(orderRepository.save(order));
    }

    private void ensureDeliveryPlanned(OrderEntity order) {
        if (order.getDeliveryId() != null) {
            return;
        }

        DeliveryDto plannedDelivery = deliveryClient.planDelivery(buildDeliveryDto(order));
        order.setDeliveryId(plannedDelivery.getDeliveryId());
    }

    private DeliveryDto buildDeliveryDto(OrderEntity order) {
        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();

        return DeliveryDto.builder()
                .deliveryId(order.getDeliveryId())
                .orderId(order.getOrderId())
                .fromAddress(warehouseAddress)
                .toAddress(toAddressDto(order.getDeliveryAddress()))
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.getFragile())
                .build();
    }

    private OrderEntity getOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(orderId));
    }

    private OrderDto toDto(OrderEntity order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .shoppingCartId(order.getShoppingCartId())
                .paymentId(order.getPaymentId())
                .deliveryId(order.getDeliveryId())
                .state(order.getState())
                .products(order.getProducts())
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.getFragile())
                .totalPrice(order.getTotalPrice())
                .productPrice(order.getProductPrice())
                .deliveryPrice(order.getDeliveryPrice())
                .build();
    }

    private OrderAddress toAddressEntity(AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }

        return OrderAddress.builder()
                .country(addressDto.getCountry())
                .city(addressDto.getCity())
                .street(addressDto.getStreet())
                .house(addressDto.getHouse())
                .flat(addressDto.getFlat())
                .build();
    }

    private AddressDto toAddressDto(OrderAddress address) {
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
}
