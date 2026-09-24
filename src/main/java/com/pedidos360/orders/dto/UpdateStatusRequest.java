package com.pedidos360.orders.dto;

import com.pedidos360.orders.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull OrderStatus status) {}