package com.pedidos360.admin;

import com.pedidos360.admin.dto.BindingRequestDto;
import com.pedidos360.admin.dto.ExchangeRequestDto;
import com.pedidos360.admin.dto.QueueInfoResponse;
import com.pedidos360.admin.dto.QueueRequestDto;
import com.pedidos360.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Lógica del microservicio administrador de RabbitMQ.
 * Toda la interacción con el broker pasa por aquí (AmqpAdmin / RabbitAdmin);
 * el controller solo recibe la petición y delega.
 *
 * Reglas de validación de negocio (además de Bean Validation en los DTO):
 *  - No se permiten nombres que empiecen con "amq." (reservados por RabbitMQ).
 *  - No se puede crear algo que ya existe (409).
 *  - No se puede eliminar algo que no existe (404).
 *  - No se pueden eliminar las colas/exchanges que usa el sistema Pedidos360 (409).
 *  - Un binding solo se crea si la cola y el exchange existen (404).
 */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final AmqpAdmin amqpAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final Set<String> recursosDelSistema;

    public RabbitAdminService(AmqpAdmin amqpAdmin,
                              RabbitTemplate rabbitTemplate,
                              @Value("${app.rabbitmq.exchange.direct}") String exDirect,
                              @Value("${app.rabbitmq.exchange.topic}") String exTopic,
                              @Value("${app.rabbitmq.queue.stock}") String qStock,
                              @Value("${app.rabbitmq.queue.notificaciones}") String qNotif,
                              @Value("${app.rabbitmq.queue.auditoria}") String qAudit,
                              @Value("${app.rabbitmq.dlq.stock}") String dlqStock,
                              @Value("${app.rabbitmq.dlq.notificaciones}") String dlqNotif,
                              @Value("${app.rabbitmq.dlq.auditoria}") String dlqAudit) {
        this.amqpAdmin = amqpAdmin;
        this.rabbitTemplate = rabbitTemplate;
        this.recursosDelSistema = Set.of(exDirect, exTopic, qStock, qNotif, qAudit, dlqStock, dlqNotif, dlqAudit);
    }

    // ===================== COLAS =====================

    public String crearCola(QueueRequestDto req) {
        String nombre = req.queueName().trim();
        validarNombreNoReservado(nombre);
        if (existeCola(nombre)) {
            throw new IllegalStateException("La cola '" + nombre + "' ya existe");
        }

        boolean durable = req.durable() == null || req.durable();
        QueueBuilder builder = durable ? QueueBuilder.durable(nombre) : QueueBuilder.nonDurable(nombre);

        if (Boolean.TRUE.equals(req.conDlq())) {
            String nombreDlq = nombre + ".dlq";
            if (existeCola(nombreDlq)) {
                throw new IllegalStateException("La DLQ '" + nombreDlq + "' ya existe");
            }
            amqpAdmin.declareQueue(QueueBuilder.durable(nombreDlq).build());
            builder.withArgument("x-dead-letter-exchange", "")
                   .withArgument("x-dead-letter-routing-key", nombreDlq);
            log.info("[ADMIN] DLQ creada: {}", nombreDlq);
        }
        if (req.ttlMs() != null) {
            builder.withArgument("x-message-ttl", req.ttlMs());
        }
        if (req.maxLength() != null) {
            builder.withArgument("x-max-length", req.maxLength());
        }

        Queue cola = builder.build();
        amqpAdmin.declareQueue(cola);
        log.info("[ADMIN] Cola creada: {} (durable={}, argumentos={})", nombre, durable, cola.getArguments());
        return nombre;
    }

    public void eliminarCola(String nombre) {
        validarNoEsDelSistema(nombre, "cola");
        if (!existeCola(nombre)) {
            throw new ResourceNotFoundException("La cola '" + nombre + "' no existe");
        }
        amqpAdmin.deleteQueue(nombre);
        log.info("[ADMIN] Cola eliminada: {}", nombre);
    }

    public QueueInfoResponse obtenerInfoCola(String nombre) {
        QueueInformation info = amqpAdmin.getQueueInfo(nombre);
        if (info == null) {
            throw new ResourceNotFoundException("La cola '" + nombre + "' no existe");
        }
        return new QueueInfoResponse(info.getName(), info.getMessageCount(), info.getConsumerCount());
    }

    // ===================== EXCHANGES =====================

    public String crearExchange(ExchangeRequestDto req) {
        String nombre = req.exchangeName().trim();
        validarNombreNoReservado(nombre);
        if (existeExchange(nombre)) {
            throw new IllegalStateException("El exchange '" + nombre + "' ya existe");
        }
        boolean durable = req.durable() == null || req.durable();
        Exchange exchange = new ExchangeBuilder(nombre, req.type()).durable(durable).build();
        amqpAdmin.declareExchange(exchange);
        log.info("[ADMIN] Exchange creado: {} (tipo={}, durable={})", nombre, req.type(), durable);
        return nombre;
    }

    public void eliminarExchange(String nombre) {
        validarNoEsDelSistema(nombre, "exchange");
        if (!existeExchange(nombre)) {
            throw new ResourceNotFoundException("El exchange '" + nombre + "' no existe");
        }
        amqpAdmin.deleteExchange(nombre);
        log.info("[ADMIN] Exchange eliminado: {}", nombre);
    }

    // ===================== BINDINGS =====================

    public String crearBinding(BindingRequestDto req) {
        validarColaYExchangeExisten(req);
        amqpAdmin.declareBinding(aBinding(req));
        String descripcion = req.exchangeName() + " --[" + req.routingKey() + "]--> " + req.queueName();
        log.info("[ADMIN] Binding creado: {}", descripcion);
        return descripcion;
    }

    public String eliminarBinding(BindingRequestDto req) {
        if (recursosDelSistema.contains(req.queueName()) && recursosDelSistema.contains(req.exchangeName())) {
            throw new IllegalStateException("No se puede eliminar un binding del sistema Pedidos360");
        }
        validarColaYExchangeExisten(req);
        amqpAdmin.removeBinding(aBinding(req));
        String descripcion = req.exchangeName() + " --[" + req.routingKey() + "]--> " + req.queueName();
        log.info("[ADMIN] Binding eliminado: {}", descripcion);
        return descripcion;
    }

    // ===================== AUXILIARES =====================

    private Binding aBinding(BindingRequestDto req) {
        return new Binding(req.queueName(), Binding.DestinationType.QUEUE,
                req.exchangeName(), req.routingKey(), null);
    }

    private void validarColaYExchangeExisten(BindingRequestDto req) {
        if (!existeCola(req.queueName())) {
            throw new ResourceNotFoundException("La cola '" + req.queueName() + "' no existe");
        }
        if (!existeExchange(req.exchangeName())) {
            throw new ResourceNotFoundException("El exchange '" + req.exchangeName() + "' no existe");
        }
    }

    private void validarNombreNoReservado(String nombre) {
        if (nombre.startsWith("amq.")) {
            throw new IllegalArgumentException("Los nombres que empiezan con 'amq.' están reservados por RabbitMQ");
        }
    }

    private void validarNoEsDelSistema(String nombre, String tipo) {
        if (recursosDelSistema.contains(nombre)) {
            throw new IllegalStateException("No se puede eliminar la " + tipo + " '" + nombre
                    + "' porque la usa el sistema Pedidos360");
        }
    }

    private boolean existeCola(String nombre) {
        return amqpAdmin.getQueueInfo(nombre) != null;
    }

    /** Declaración pasiva: si el exchange no existe RabbitMQ responde 404 y devolvemos false. */
    private boolean existeExchange(String nombre) {
        try {
            rabbitTemplate.execute(channel -> channel.exchangeDeclarePassive(nombre));
            return true;
        } catch (AmqpException e) {
            return false;
        }
    }
}
