package ru.yandex.practicum.commerce.warehouse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.api.dto.AddressDto;
import ru.yandex.practicum.commerce.api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.api.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.warehouse.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};

    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    private final WarehouseProductRepository warehouseProductRepository;

    @Transactional
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        UUID productId = request.getProductId();

        if (warehouseProductRepository.existsById(productId)) {
            throw new SpecifiedProductAlreadyInWarehouseException(productId);
        }

        WarehouseProduct product = WarehouseProduct.builder()
                .productId(productId)
                .fragile(Boolean.TRUE.equals(request.getFragile()))
                .width(request.getDimension().getWidth())
                .height(request.getDimension().getHeight())
                .depth(request.getDimension().getDepth())
                .weight(request.getWeight())
                .quantity(0L)
                .build();

        warehouseProductRepository.save(product);
    }

    @Transactional
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        WarehouseProduct product = warehouseProductRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(request.getProductId()));

        product.setQuantity(product.getQuantity() + request.getQuantity());
        warehouseProductRepository.save(product);
    }

    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        Map<UUID, Long> missingProducts = new HashMap<>();

        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : shoppingCartDto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long requestedQuantity = entry.getValue();

            WarehouseProduct product = warehouseProductRepository.findById(productId).orElse(null);

            if (product == null) {
                missingProducts.put(productId, requestedQuantity);
                continue;
            }

            long availableQuantity = product.getQuantity();

            if (availableQuantity < requestedQuantity) {
                missingProducts.put(productId, requestedQuantity - availableQuantity);
                continue;
            }

            totalWeight += product.getWeight() * requestedQuantity;
            totalVolume += product.getWidth() * product.getHeight() * product.getDepth() * requestedQuantity;
            fragile = fragile || Boolean.TRUE.equals(product.getFragile());
        }

        if (!missingProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse(missingProducts);
        }

        return BookedProductsDto.builder()
                .deliveryWeight(totalWeight)
                .deliveryVolume(totalVolume)
                .fragile(fragile)
                .build();
    }

    public AddressDto getWarehouseAddress() {
        return AddressDto.builder()
                .country(CURRENT_ADDRESS)
                .city(CURRENT_ADDRESS)
                .street(CURRENT_ADDRESS)
                .house(CURRENT_ADDRESS)
                .flat(CURRENT_ADDRESS)
                .build();
    }
}