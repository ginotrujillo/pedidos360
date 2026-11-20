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
 * Consumer del dominio STOCK.
 * Escucha pedidos.stock.queue (exchange direct, routing key pedido.aceptado.evento).
 *
 * ACK MANUAL (acknowledge-mode: manual):
 *  - Si el procesamiento termina bien  -> channel.basicAck   (RabbitMQ borra el mensaje).
 *  - Si ocurre cualquier error         -> channel.basicNack  con requeue=false,
 *    por lo que RabbitMQ lo envía a la DLQ configurada en la cola (x-dead-letter-*).
 */
@Component
public class StockEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockEventConsumer.class);

    @Value("${app.rabbitmq.dlq.stock}")
    private String dlq;

    @RabbitListener(queues = "${app.rabbitmq.queue.stock}")
    public void recibir(PedidoEventoDto evento,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("[STOCK] Procesando evento de pedido {} - estado {}", evento.pedidoId(), evento.estado());

            if (evento.total() == null || evento.total().signum() <= 0) {
                throw new IllegalStateException(
                        "Evento de stock inválido para pedido " + evento.pedidoId() + ": total no es positivo");
            }

            // Simulación de la confirmación del descuento de stock (el descuento real ya ocurrió
            // de forma síncrona en OrderService; este consumer representa un proceso asíncrono
            // adicional, por ejemplo avisar al sistema de inventario).
            log.info("[STOCK] Confirmado el descuento de stock para el pedido {} (total: {})",
                    evento.pedidoId(), evento.total());

            channel.basicAck(deliveryTag, false);
            log.info("[STOCK] ACK enviado (deliveryTag={})", deliveryTag);

        } catch (Exception e) {
            log.error("[STOCK] ERROR procesando pedido {}: {} -> NACK sin reencolar, el mensaje va a la DLQ '{}'",
                    evento.pedidoId(), e.getMessage(), dlq);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
