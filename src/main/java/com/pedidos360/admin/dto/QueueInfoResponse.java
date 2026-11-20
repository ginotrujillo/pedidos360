package com.pedidos360.admin.dto;

/** Estado de una cola: mensajes en espera (Ready) y consumidores conectados. */
public record QueueInfoResponse(String queueName, long mensajesEnEspera, int consumidores) {
}
