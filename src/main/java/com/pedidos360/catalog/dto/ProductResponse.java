package com.pedidos360.catalog.dto;

import com.pedidos360.catalog.entity.Product;
import java.math.BigDecimal;

public record ProductResponse(
        Long id, String name, String description, BigDecimal price, Integer stock
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock());
    }
}