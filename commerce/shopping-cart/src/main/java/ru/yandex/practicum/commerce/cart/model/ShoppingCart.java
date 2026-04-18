package ru.yandex.practicum.commerce.cart.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "shopping_carts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCart {

    @Id
    @Column(name = "shopping_cart_id", nullable = false)
    private UUID shoppingCartId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "shopping_cart_products",
            joinColumns = @JoinColumn(name = "shopping_cart_id")
    )
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Map<UUID, Long> products = new HashMap<>();
}