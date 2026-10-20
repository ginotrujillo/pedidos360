package com.pedidos360.messaging.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoEventoDto(
        Long pedidoId,
        String estado,
        String clienteEmail,
        BigDecimal total,
        LocalDateTime timestamp
) implements Serializable {
}