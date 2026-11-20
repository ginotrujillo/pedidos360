package com.pedidos360.admin.dto;

/** Respuesta simple de las operaciones del administrador. */
public record AdminResponse(String operacion, String recurso, String mensaje) {
}
