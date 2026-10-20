package com.pedidos360.admin;

import jakarta.validation.constraints.NotBlank;

public record QueueRequestDto(
    @NotBlank(message = "El nombre de la cola no puede estar vacío ni contener solo espacios")
    String queueName
) {}