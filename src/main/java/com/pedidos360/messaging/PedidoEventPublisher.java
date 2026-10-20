package com.pedidos360.messaging;

import com.pedidos360.messaging.dto.PedidoEventoDto;
import com.pedidos360.orders.entity.Order;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PedidoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.direct}") private String directExchange;
    @Value("${app.rabbitmq.exchange.topic}") private String topicExchange;

    /** Publica el evento de cambio de estado en ambos exchanges, con la routing key correspondiente al estado. */
    public void publicarCambioEstado(Order order) {
        String estado = order.getStatus().name().toLowerCase();
        PedidoEventoDto evento = new PedidoEventoDto(
                order.getId(), order.getStatus().name(), order.getCustomerEmail(),
                order.getTotal(), LocalDateTime.now());

        String routingKeyTopic = "pedido." + estado + ".evento";

        // Exchange topic: lo reciben notificaciones (pedido.*.evento) y auditoria (pedido.#)
        rabbitTemplate.convertAndSend(topicExchange, routingKeyTopic, evento);
        log.info("Publicado evento '{}' para pedido {} en exchange topic", routingKeyTopic, order.getId());

        // Exchange direct: solo cuando el estado es ACEPTADO, para el flujo de stock
        if ("aceptado".equals(estado)) {
            rabbitTemplate.convertAndSend(directExchange, "pedido.aceptado.evento", evento);
            log.info("Publicado evento 'pedido.aceptado.evento' para pedido {} en exchange direct", order.getId());
        }
    }
}