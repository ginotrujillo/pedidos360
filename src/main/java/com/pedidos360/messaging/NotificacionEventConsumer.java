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
 * Consumer del dominio NOTIFICACIONES.
 * Escucha pedidos.notificaciones.queue (exchange topic, routing key pedido.*.evento).
 * ACK manual: basicAck si todo sale bien, basicNack(requeue=false) -> DLQ si falla.
 */
@Component
public class NotificacionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionEventConsumer.class);

    @Value("${app.rabbitmq.dlq.notificaciones}")
    private String dlq;

    /** Demora opcional (ms) para poder ver mensajes "Unacked" en la Management UI. 0 = sin demora. */
    @Value("${app.rabbitmq.demo.demora-ms:0}")
    private long demoraMs;

    @RabbitListener(queues = "${app.rabbitmq.queue.notificaciones}")
    public void recibir(PedidoEventoDto evento,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("[NOTIFICACIONES] Procesando evento de pedido {} - estado {}", evento.pedidoId(), evento.estado());

            if (evento.clienteEmail() == null || evento.clienteEmail().isBlank()) {
                throw new IllegalStateException(
                        "Evento de notificacion sin email de cliente para pedido " + evento.pedidoId());
            }

            if (demoraMs > 0) {
                Thread.sleep(demoraMs); // solo para la demostración de métricas (Unacked)
            }

            // Simulación del envío de la notificación (correo/SMS) al cliente
            log.info("[NOTIFICACIONES] Enviado aviso a {} -> pedido {} ahora está {}",
                    evento.clienteEmail(), evento.pedidoId(), evento.estado());

            channel.basicAck(deliveryTag, false);
            log.info("[NOTIFICACIONES] ACK enviado (deliveryTag={})", deliveryTag);

        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("[NOTIFICACIONES] ERROR procesando pedido {}: {} -> NACK sin reencolar, el mensaje va a la DLQ '{}'",
                    evento.pedidoId(), e.getMessage(), dlq);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
