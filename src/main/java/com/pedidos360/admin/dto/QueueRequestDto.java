package com.pedidos360.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear una cola desde el administrador.
 *
 * @param queueName nombre de la cola (letras, números, punto, guion y guion bajo)
 * @param durable   si sobrevive a un reinicio del broker (por defecto true)
 * @param conDlq    si es true se crea también "<nombre>.dlq" y la cola queda enlazada a ella
 * @param ttlMs     (opcional) tiempo de vida de los mensajes en milisegundos
 * @param maxLength (opcional) cantidad máxima de mensajes en la cola
 */
public record QueueRequestDto(
        @NotBlank(message = "El nombre de la cola es obligatorio")
        @Size(max = 200, message = "El nombre de la cola no puede superar 200 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "El nombre de la cola solo puede tener letras, números, '.', '-' y '_'")
        String queueName,

        Boolean durable,

        Boolean conDlq,

        @Positive(message = "ttlMs debe ser mayor que 0")
        @Max(value = 604800000L, message = "ttlMs no puede superar 7 días (604800000 ms)")
        Long ttlMs,

        @Positive(message = "maxLength debe ser mayor que 0")
        @Max(value = 100000, message = "maxLength no puede superar 100000 mensajes")
        Integer maxLength
) {
}
