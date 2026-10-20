package com.pedidos360.messaging;

import com.pedidos360.messaging.dto.PedidoEventoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionEventConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.queue.notificaciones}")
    public void recibir(PedidoEventoDto evento) {
        log.info("[NOTIFICACIONES] Procesando evento de pedido {} - estado {}", evento.pedidoId(), evento.estado());

        if (evento.clienteEmail() == null || evento.clienteEmail().isBlank()) {
            // Sin email no se puede notificar: forzamos el fallo para que vaya a la DLQ
            throw new IllegalStateException(
                    "Evento de notificacion sin email de cliente para pedido " + evento.pedidoId());
        }

        // Simulación del envío de la notificación (correo/SMS) al cliente
        log.info("[NOTIFICACIONES] Enviado aviso a {} -> pedido {} ahora está {}",
                evento.clienteEmail(), evento.pedidoId(), evento.estado());
    }
}
