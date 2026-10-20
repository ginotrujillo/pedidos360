package com.pedidos360.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange.direct}")
    private String directExchangeName;
    @Value("${app.rabbitmq.exchange.topic}")
    private String topicExchangeName;

    @Value("${app.rabbitmq.queue.stock}")
    private String stockQueue;
    @Value("${app.rabbitmq.queue.notificaciones}")
    private String notificacionesQueue;
    @Value("${app.rabbitmq.queue.auditoria}")
    private String auditoriaQueue;

    @Value("${app.rabbitmq.dlq.stock}")
    private String stockDlq;
    @Value("${app.rabbitmq.dlq.notificaciones}")
    private String notificacionesDlq;
    @Value("${app.rabbitmq.dlq.auditoria}")
    private String auditoriaDlq;

    @Value("${app.rabbitmq.routing.aceptado}")
    private String rkAceptado;
    @Value("${app.rabbitmq.routing.wildcardEstado}")
    private String rkWildcardEstado;
    @Value("${app.rabbitmq.routing.wildcardTodo}")
    private String rkWildcardTodo;

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // ---- Administrador e Inicialización Forzada ----
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    public ApplicationRunner initializeRabbit(RabbitAdmin rabbitAdmin) {
        return args -> {
            rabbitAdmin.initialize();
        };
    }

    // ---- Exchanges ----
    @Bean
    DirectExchange directExchange() {
        return ExchangeBuilder.directExchange(directExchangeName).durable(true).build();
    }

    @Bean
    TopicExchange topicExchange() {
        return ExchangeBuilder.topicExchange(topicExchangeName).durable(true).build();
    }

    // ---- DLQ (colas de error, sin dead-letter propio) ----
    @Bean
    Queue stockDlq() {
        return QueueBuilder.durable(stockDlq).build();
    }

    @Bean
    Queue notificacionesDlq() {
        return QueueBuilder.durable(notificacionesDlq).build();
    }

    @Bean
    Queue auditoriaDlq() {
        return QueueBuilder.durable(auditoriaDlq).build();
    }

    // ---- Colas principales (cada una apunta a su DLQ) ----
    @Bean
    Queue stockQueue() {
        return QueueBuilder.durable(stockQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", stockDlq)
                .build();
    }

    @Bean
    Queue notificacionesQueue() {
        return QueueBuilder.durable(notificacionesQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", notificacionesDlq)
                .build();
    }

    @Bean
    Queue auditoriaQueue() {
        return QueueBuilder.durable(auditoriaQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", auditoriaDlq)
                .build();
    }

    // ---- Bindings ----
    @Bean
    Binding bindStock(Queue stockQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(stockQueue).to(directExchange).with(rkAceptado);
    }

    @Bean
    Binding bindNotificaciones(Queue notificacionesQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(notificacionesQueue).to(topicExchange).with(rkWildcardEstado);
    }

    @Bean
    Binding bindAuditoria(Queue auditoriaQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(auditoriaQueue).to(topicExchange).with(rkWildcardTodo);
    }
}