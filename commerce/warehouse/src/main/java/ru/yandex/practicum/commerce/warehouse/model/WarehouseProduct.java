package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "warehouse_products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseProduct {

    @Id
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "fragile", nullable = false)
    private Boolean fragile;

    @Column(name = "width", nullable = false)
    private Double width;

    @Column(name = "height", nullable = false)
    private Double height;

    @Column(name = "depth", nullable = false)
    private Double depth;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "quantity", nullable = false)
    private Long quantity;
}