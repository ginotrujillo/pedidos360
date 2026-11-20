package com.pedidos360.admin;

import com.pedidos360.admin.dto.AdminResponse;
import com.pedidos360.admin.dto.BindingRequestDto;
import com.pedidos360.admin.dto.ExchangeRequestDto;
import com.pedidos360.admin.dto.QueueInfoResponse;
import com.pedidos360.admin.dto.QueueRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Microservicio administrador de RabbitMQ (solo rol Admin).
 *
 *  POST   /api/admin/rabbitmq/queues            crear cola (opcional: DLQ, TTL, max-length)
 *  GET    /api/admin/rabbitmq/queues/{name}     ver mensajes en espera y consumidores
 *  DELETE /api/admin/rabbitmq/queues/{name}     eliminar cola
 *  POST   /api/admin/rabbitmq/exchanges         crear exchange (direct, topic, fanout)
 *  DELETE /api/admin/rabbitmq/exchanges/{name}  eliminar exchange
 *  POST   /api/admin/rabbitmq/bindings          crear binding cola <- exchange
 *  DELETE /api/admin/rabbitmq/bindings          eliminar binding (mismo body que al crear)
 */
@RestController
@RequestMapping("/api/admin/rabbitmq")
@PreAuthorize("hasRole('Admin')")
public class RabbitAdminController {

    private static final String NOMBRE_VALIDO = "^[a-zA-Z0-9._-]+$";
    private static final String MSG_NOMBRE = "Nombre inválido: solo letras, números, '.', '-' y '_'";

    private final RabbitAdminService rabbitAdminService;

    public RabbitAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    // ---------- Colas ----------
    @PostMapping("/queues")
    public ResponseEntity<AdminResponse> crearCola(@Valid @RequestBody QueueRequestDto request) {
        String nombre = rabbitAdminService.crearCola(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AdminResponse("CREAR_COLA", nombre, "Cola creada correctamente"));
    }

    @GetMapping("/queues/{name}")
    public QueueInfoResponse verCola(@PathVariable("name") @NotBlank @Pattern(regexp = NOMBRE_VALIDO, message = MSG_NOMBRE) String name) {
        return rabbitAdminService.obtenerInfoCola(name);
    }

    @DeleteMapping("/queues/{name}")
    public AdminResponse eliminarCola(@PathVariable("name") @NotBlank @Pattern(regexp = NOMBRE_VALIDO, message = MSG_NOMBRE) String name) {
        rabbitAdminService.eliminarCola(name);
        return new AdminResponse("ELIMINAR_COLA", name, "Cola eliminada correctamente");
    }

    // ---------- Exchanges ----------
    @PostMapping("/exchanges")
    public ResponseEntity<AdminResponse> crearExchange(@Valid @RequestBody ExchangeRequestDto request) {
        String nombre = rabbitAdminService.crearExchange(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AdminResponse("CREAR_EXCHANGE", nombre, "Exchange creado correctamente"));
    }

    @DeleteMapping("/exchanges/{name}")
    public AdminResponse eliminarExchange(@PathVariable("name") @NotBlank @Pattern(regexp = NOMBRE_VALIDO, message = MSG_NOMBRE) String name) {
        rabbitAdminService.eliminarExchange(name);
        return new AdminResponse("ELIMINAR_EXCHANGE", name, "Exchange eliminado correctamente");
    }

    // ---------- Bindings ----------
    @PostMapping("/bindings")
    public ResponseEntity<AdminResponse> crearBinding(@Valid @RequestBody BindingRequestDto request) {
        String binding = rabbitAdminService.crearBinding(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AdminResponse("CREAR_BINDING", binding, "Binding creado correctamente"));
    }

    @DeleteMapping("/bindings")
    public AdminResponse eliminarBinding(@Valid @RequestBody BindingRequestDto request) {
        String binding = rabbitAdminService.eliminarBinding(request);
        return new AdminResponse("ELIMINAR_BINDING", binding, "Binding eliminado correctamente");
    }
}
