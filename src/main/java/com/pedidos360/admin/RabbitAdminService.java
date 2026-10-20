package com.pedidos360.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);
    private final AmqpAdmin amqpAdmin;

    public RabbitAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    public void crearCola(String queueName) {
        // Crea una cola duradera normal. 
        // (Opcional: puedes agregar lógica aquí para vincularla a un Dead Letter Exchange si lo deseas)
        Queue nuevaCola = QueueBuilder.durable(queueName).build();
        amqpAdmin.declareQueue(nuevaCola);
        log.info("Cola creada exitosamente desde el administrador: {}", queueName);
    }

    public void eliminarCola(String queueName) {
        amqpAdmin.deleteQueue(queueName);
        log.info("Cola eliminada exitosamente desde el administrador: {}", queueName);
    }
}