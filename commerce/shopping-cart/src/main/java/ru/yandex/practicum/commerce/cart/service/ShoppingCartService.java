package ru.yandex.practicum.commerce.cart.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.cart.client.WarehouseClient;
import ru.yandex.practicum.commerce.cart.exception.ProductNotFoundInShoppingCartException;
import ru.yandex.practicum.commerce.cart.exception.ProductQuantityNotEnoughInWarehouseException;
import ru.yandex.practicum.commerce.cart.mapper.ShoppingCartMapper;
import ru.yandex.practicum.commerce.cart.model.ShoppingCart;
import ru.yandex.practicum.commerce.cart.repository.ShoppingCartRepository;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final WarehouseClient warehouseClient;

    public ShoppingCartDto getShoppingCart(String username) {
        ShoppingCart cart = getOrCreateActiveCart(username);
        return shoppingCartMapper.toDto(cart);
    }

    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        ShoppingCart cart = getOrCreateActiveCart(username);

        Map<UUID, Long> mergedProducts = new HashMap<>(cart.getProducts());

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            mergedProducts.merge(entry.getKey(), entry.getValue(), Long::sum);
        }

        validateProductsInWarehouse(cart.getShoppingCartId(), mergedProducts);

        cart.setProducts(mergedProducts);
        shoppingCartRepository.save(cart);

        return shoppingCartMapper.toDto(cart);
    }

    @Transactional
    public void deactivateCurrentShoppingCart(String username) {
        shoppingCartRepository.findFirstByUsernameAndActiveTrue(username)
                .ifPresent(cart -> {
                    cart.setActive(false);
                    shoppingCartRepository.save(cart);
                });
    }

    @Transactional
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        ShoppingCart cart = getOrCreateActiveCart(username);

        Map<UUID, Long> updatedProducts = new HashMap<>(cart.getProducts());
        productIds.forEach(updatedProducts::remove);

        cart.setProducts(updatedProducts);
        shoppingCartRepository.save(cart);

        return shoppingCartMapper.toDto(cart);
    }

    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        ShoppingCart cart = getOrCreateActiveCart(username);

        Map<UUID, Long> updatedProducts = new HashMap<>(cart.getProducts());

        if (!updatedProducts.containsKey(request.getProductId())) {
            throw new ProductNotFoundInShoppingCartException(request.getProductId());
        }

        updatedProducts.put(request.getProductId(), request.getNewQuantity());

        validateProductsInWarehouse(cart.getShoppingCartId(), updatedProducts);

        cart.setProducts(updatedProducts);
        shoppingCartRepository.save(cart);

        return shoppingCartMapper.toDto(cart);
    }

    @Transactional
    protected ShoppingCart getOrCreateActiveCart(String username) {
        return shoppingCartRepository.findFirstByUsernameAndActiveTrue(username)
                .orElseGet(() -> shoppingCartRepository.save(
                        ShoppingCart.builder()
                                .shoppingCartId(UUID.randomUUID())
                                .username(username)
                                .active(true)
                                .products(new HashMap<>())
                                .build()
                ));
    }

    private void validateProductsInWarehouse(UUID shoppingCartId, Map<UUID, Long> products) {
        try {
            warehouseClient.checkProductQuantityEnoughForShoppingCart(
                    ShoppingCartDto.builder()
                            .shoppingCartId(shoppingCartId)
                            .products(products)
                            .build()
            );
        } catch (FeignException.BadRequest e) {
            String message = e.contentUTF8();
            if (message == null || message.isBlank()) {
                message = new String(e.responseBody().orElseThrow().array(), StandardCharsets.UTF_8);
            }
            throw new ProductQuantityNotEnoughInWarehouseException(message);
        }
    }
}