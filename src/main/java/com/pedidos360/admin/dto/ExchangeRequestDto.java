package com.pedidos360.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear un exchange desde el administrador.
 *
 * @param exchangeName nombre del exchange
 * @param type         tipo: direct, topic o fanout
 * @param durable      si sobrevive a un reinicio del broker (por defecto true)
 */
public record ExchangeRequestDto(
        @NotBlank(message = "El nombre del exchange es obligatorio")
        @Size(max = 200, message = "El nombre del exchange no puede superar 200 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "El nombre del exchange solo puede tener letras, números, '.', '-' y '_'")
        String exchangeName,

        @NotBlank(message = "El tipo de exchange es obligatorio")
        @Pattern(regexp = "^(direct|topic|fanout)$",
                message = "Tipo de exchange inválido: use direct, topic o fanout")
        String type,

        Boolean durable
) {
}
