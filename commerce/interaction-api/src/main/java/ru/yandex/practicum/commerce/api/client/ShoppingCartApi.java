package ru.yandex.practicum.commerce.api.client;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.api.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.api.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/api/v1/shopping-cart")
public interface ShoppingCartApi {

    @GetMapping
    ShoppingCartDto getShoppingCart(@RequestParam String username);

    @PutMapping
    ShoppingCartDto addProductToShoppingCart(@RequestParam String username,
                                             @RequestBody Map<UUID, Long> products);

    @DeleteMapping
    void deactivateCurrentShoppingCart(@RequestParam String username);

    @PostMapping("/remove")
    ShoppingCartDto removeFromShoppingCart(@RequestParam String username,
                                           @RequestBody List<UUID> productIds);

    @PostMapping("/change-quantity")
    ShoppingCartDto changeProductQuantity(@RequestParam String username,
                                          @Valid @RequestBody ChangeProductQuantityRequest request);
}