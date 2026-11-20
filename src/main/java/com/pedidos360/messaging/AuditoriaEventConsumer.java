package com.pedidos360.messaging;

import com.pedidos360.messaging.dto.PedidoEventoDto;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumer del dominio AUDITORIA.
 * Escucha pedidos.auditoria.queue (exchange topic, routing key pedido.#).
 * ACK manual: basicAck si todo sale bien, basicNack(requeue=false) -> DLQ si falla.
 */
@Component
public class AuditoriaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaEventConsumer.class);

    @Value("${app.rabbitmq.dlq.auditoria}")
    private String dlq;

    @RabbitListener(queues = "${app.rabbitmq.queue.auditoria}")
    public void recibir(PedidoEventoDto evento,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("[AUDITORIA] Registrando evento: pedido={} estado={} total={} timestamp={}",
                    evento.pedidoId(), evento.estado(), evento.total(), evento.timestamp());

            if (evento.pedidoId() == null) {
                throw new IllegalStateException("Evento de auditoria sin pedidoId");
            }

            // En una versión más completa se guardaría en una tabla de auditoría.
            log.info("[AUDITORIA] Evento de pedido {} registrado correctamente", evento.pedidoId());

            channel.basicAck(deliveryTag, false);
            log.info("[AUDITORIA] ACK enviado (deliveryTag={})", deliveryTag);

        } catch (Exception e) {
            log.error("[AUDITORIA] ERROR procesando pedido {}: {} -> NACK sin reencolar, el mensaje va a la DLQ '{}'",
                    evento.pedidoId(), e.getMessage(), dlq);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
