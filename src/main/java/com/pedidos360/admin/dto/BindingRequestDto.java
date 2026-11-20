package com.pedidos360.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o eliminar un binding (cola <- exchange con una routing key).
 * Para un exchange fanout la routing key puede ir vacía ("").
 */
public record BindingRequestDto(
        @NotBlank(message = "El nombre de la cola es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Nombre de cola inválido")
        String queueName,

        @NotBlank(message = "El nombre del exchange es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Nombre de exchange inválido")
        String exchangeName,

        @NotNull(message = "La routing key es obligatoria (puede ser \"\" para fanout)")
        @Size(max = 255, message = "La routing key no puede superar 255 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._*#-]*$",
                message = "La routing key solo puede tener letras, números, '.', '-', '_', '*' y '#'")
        String routingKey
) {
}
