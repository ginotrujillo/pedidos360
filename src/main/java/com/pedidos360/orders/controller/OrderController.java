package com.pedidos360.orders.controller;

import com.pedidos360.orders.dto.*;
import com.pedidos360.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest req,
                                @AuthenticationPrincipal Jwt jwt) {
        return service.create(req, jwt);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    public List<OrderResponse> list(@AuthenticationPrincipal Jwt jwt, Authentication auth) {
        return service.list(jwt, auth);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Operador','Cliente')")
    public OrderResponse get(@PathVariable Long id,
                             @AuthenticationPrincipal Jwt jwt, Authentication auth) {
        return service.get(id, jwt, auth);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('Admin','Operador')")
    public OrderResponse updateStatus(@PathVariable Long id,
                                      @Valid @RequestBody UpdateStatusRequest req) {
        return service.updateStatus(id, req.status());
    }
}