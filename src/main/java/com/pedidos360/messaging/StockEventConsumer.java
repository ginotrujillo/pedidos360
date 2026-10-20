package com.pedidos360.messaging;

import com.pedidos360.messaging.dto.PedidoEventoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StockEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockEventConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.queue.stock}")
    public void recibir(PedidoEventoDto evento) {
        log.info("[STOCK] Procesando evento de pedido {} - estado {}", evento.pedidoId(), evento.estado());

        if (evento.total() == null || evento.total().signum() <= 0) {
            // Dato inválido: forzamos el fallo para que el mensaje caiga a la DLQ
            throw new IllegalStateException(
                    "Evento de stock inválido para pedido " + evento.pedidoId() + ": total no es positivo");
        }

        // Simulación de la confirmación del descuento de stock (el descuento real ya ocurrió
        // de forma síncrona en OrderService; este consumer representa un proceso asíncrono
        // adicional, por ejemplo una notificación al sistema de inventario).
        log.info("[STOCK] Confirmado el descuento de stock para el pedido {} (total: {})",
                evento.pedidoId(), evento.total());
    }
}
