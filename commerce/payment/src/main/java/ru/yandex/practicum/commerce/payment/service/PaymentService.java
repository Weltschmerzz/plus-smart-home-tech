package ru.yandex.practicum.commerce.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.dto.OrderDto;
import ru.yandex.practicum.commerce.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.api.dto.ProductDto;
import ru.yandex.practicum.commerce.api.enumtype.PaymentState;
import ru.yandex.practicum.commerce.payment.client.OrderClient;
import ru.yandex.practicum.commerce.payment.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.payment.exception.PaymentNotFoundException;
import ru.yandex.practicum.commerce.payment.model.Payment;
import ru.yandex.practicum.commerce.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.10);
    private static final int MONEY_SCALE = 2;

    private final PaymentRepository paymentRepository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    public BigDecimal productCost(OrderDto orderDto) {
        BigDecimal productTotal = BigDecimal.ZERO;

        for (Map.Entry<UUID, Long> entry : orderDto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();

            ProductDto product = shoppingStoreClient.getProduct(productId);

            BigDecimal currentProductCost = product.getPrice()
                    .multiply(BigDecimal.valueOf(quantity));

            productTotal = productTotal.add(currentProductCost);
        }

        return scale(productTotal);
    }

    public BigDecimal getTotalCost(OrderDto orderDto) {
        BigDecimal productTotal = productCost(orderDto);
        BigDecimal deliveryTotal = normalizeMoney(orderDto.getDeliveryPrice());

        return calculateTotalCost(productTotal, deliveryTotal);
    }

    @Transactional
    public PaymentDto payment(OrderDto orderDto) {
        BigDecimal productTotal = productCost(orderDto);
        BigDecimal deliveryTotal = normalizeMoney(orderDto.getDeliveryPrice());
        BigDecimal totalPayment = calculateTotalCost(productTotal, deliveryTotal);

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID())
                .orderId(orderDto.getOrderId())
                .productTotal(productTotal)
                .deliveryTotal(deliveryTotal)
                .totalPayment(totalPayment)
                .paymentState(PaymentState.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return toDto(savedPayment);
    }

    @Transactional
    public void paymentSuccess(UUID paymentId) {
        Payment payment = getPaymentOrThrow(paymentId);

        payment.setPaymentState(PaymentState.SUCCESS);
        paymentRepository.save(payment);

        orderClient.payment(payment.getOrderId());
    }

    @Transactional
    public void paymentFailed(UUID paymentId) {
        Payment payment = getPaymentOrThrow(paymentId);

        payment.setPaymentState(PaymentState.FAILED);
        paymentRepository.save(payment);

        orderClient.paymentFailed(payment.getOrderId());
    }

    private Payment getPaymentOrThrow(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    private BigDecimal calculateTotalCost(BigDecimal productTotal, BigDecimal deliveryTotal) {
        BigDecimal tax = productTotal.multiply(TAX_RATE);

        return scale(productTotal.add(tax).add(deliveryTotal));
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }

        return scale(value);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private PaymentDto toDto(Payment payment) {
        return PaymentDto.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .productTotal(payment.getProductTotal())
                .deliveryTotal(payment.getDeliveryTotal())
                .totalPayment(payment.getTotalPayment())
                .paymentState(payment.getPaymentState())
                .build();
    }
}