package ru.yandex.practicum.commerce.api.client;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.api.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.api.dto.AddressDto;
import ru.yandex.practicum.commerce.api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.api.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.api.dto.ShoppingCartDto;

@RequestMapping("/api/v1/warehouse")
public interface WarehouseApi {

    @PutMapping
    void newProductInWarehouse(@Valid @RequestBody NewProductInWarehouseRequest request);

    @PostMapping("/check")
    BookedProductsDto checkProductQuantityEnoughForShoppingCart(
            @Valid @RequestBody ShoppingCartDto shoppingCartDto
    );

    @PostMapping("/add")
    void addProductToWarehouse(@Valid @RequestBody AddProductToWarehouseRequest request);

    @GetMapping("/address")
    AddressDto getWarehouseAddress();
}