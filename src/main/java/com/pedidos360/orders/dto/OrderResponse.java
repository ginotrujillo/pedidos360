package com.pedidos360.orders.dto;

import com.pedidos360.orders.entity.Order;
import com.pedidos360.orders.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerName,
        String customerEmail,
        OrderStatus status,
        LocalDateTime createdAt,
        BigDecimal total,
        List<ItemResponse> items
) {
    public record ItemResponse(
            Long productId, String productName,
            Integer quantity, BigDecimal unitPrice, BigDecimal subtotal
    ) {}

    public static OrderResponse from(Order o) {
        List<ItemResponse> items = o.getItems().stream()
                .map(i -> new ItemResponse(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                .toList();
        return new OrderResponse(o.getId(), o.getCustomerName(), o.getCustomerEmail(),
                o.getStatus(), o.getCreatedAt(), o.getTotal(), items);
    }
}