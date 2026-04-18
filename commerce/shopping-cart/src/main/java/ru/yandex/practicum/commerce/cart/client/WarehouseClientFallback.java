package ru.yandex.practicum.commerce.cart.client;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.cart.exception.WarehouseServiceUnavailableException;

@Component
public class WarehouseClientFallback implements WarehouseClient {

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        throw new WarehouseServiceUnavailableException(
                "Сервис склада временно недоступен. Попробуйте добавить товар позже."
        );
    }
}