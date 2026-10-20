package com.pedidos360.messaging;

import com.pedidos360.messaging.dto.PedidoEventoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaEventConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.queue.auditoria}")
    public void recibir(PedidoEventoDto evento) {
        log.info("[AUDITORIA] Registrando evento: pedido={} estado={} total={} timestamp={}",
                evento.pedidoId(), evento.estado(), evento.total(), evento.timestamp());

        if (evento.pedidoId() == null) {
            // Evento sin identificador de pedido: no se puede auditar, va a la DLQ
            throw new IllegalStateException("Evento de auditoria sin pedidoId");
        }

        // Aquí, en una versión más completa, se guardaría en una tabla de auditoría.
        // Para la EP3 basta con dejar constancia en el log de que el evento fue procesado.
        log.info("[AUDITORIA] Evento de pedido {} registrado correctamente", evento.pedidoId());
    }
}
