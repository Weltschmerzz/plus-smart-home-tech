package ru.yandex.practicum.commerce.cart.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.cart.model.ShoppingCart;

import java.util.HashMap;

@Component
public class ShoppingCartMapper {

    public ShoppingCartDto toDto(ShoppingCart shoppingCart) {
        return ShoppingCartDto.builder()
                .shoppingCartId(shoppingCart.getShoppingCartId())
                .products(new HashMap<>(shoppingCart.getProducts()))
                .build();
    }
}