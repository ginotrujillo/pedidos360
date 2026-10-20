package com.pedidos360.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/rabbitmq")
public class RabbitAdminController {

    private final RabbitAdminService rabbitAdminService;

    public RabbitAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    @PostMapping("/queues")
    public ResponseEntity<String> crearCola(@Valid @RequestBody QueueRequestDto request) {
        rabbitAdminService.crearCola(request.queueName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Cola '" + request.queueName() + "' creada con éxito.");
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<String> eliminarCola(@PathVariable("name") String name) {
        rabbitAdminService.eliminarCola(name);
        return ResponseEntity.ok("Cola '" + name + "' eliminada con éxito (si existía).");
    }
}