package com.pedidos360.orders.service;

import com.pedidos360.catalog.entity.Product;
import com.pedidos360.catalog.repository.ProductRepository;
import com.pedidos360.common.exception.ResourceNotFoundException;
import com.pedidos360.messaging.PedidoEventPublisher;
import com.pedidos360.orders.dto.CreateOrderRequest;
import com.pedidos360.orders.dto.OrderResponse;
import com.pedidos360.orders.entity.Order;
import com.pedidos360.orders.entity.OrderItem;
import com.pedidos360.orders.entity.OrderStatus;
import com.pedidos360.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orders;
    private final ProductRepository products;
    private final PedidoEventPublisher eventPublisher;

    @Transactional
    public OrderResponse create(CreateOrderRequest req, Jwt jwt) {
        Order order = new Order();
        order.setCustomerId(jwt.getClaimAsString("oid"));
        order.setCustomerName(jwt.getClaimAsString("name"));
        order.setCustomerEmail(jwt.getClaimAsString("preferred_username"));

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderRequest.Item item : req.items()) {
            Product p = products.findById(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto " + item.productId() + " no encontrado"));

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(item.quantity());
            oi.setUnitPrice(p.getPrice());
            order.getItems().add(oi);

            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(item.quantity())));
        }
        order.setTotal(total);
        Order saved = orders.save(order);
        eventPublisher.publicarCambioEstado(saved);
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list(Jwt jwt, Authentication auth) {
        List<Order> result = seesAll(auth)
                ? orders.findAllByOrderByCreatedAtDesc()
                : orders.findByCustomerIdOrderByCreatedAtDesc(jwt.getClaimAsString("oid"));
        return result.stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id, Jwt jwt, Authentication auth) {
        Order o = find(id);
        if (!seesAll(auth) && !o.getCustomerId().equals(jwt.getClaimAsString("oid"))) {
            throw new AccessDeniedException("No tienes acceso a este pedido");
        }
        return OrderResponse.from(o);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus next) {
        Order o = find(id);
        OrderStatus current = o.getStatus();

        // Regla mÃ­nima de la pauta
        if (next == OrderStatus.DESPACHADO && current == OrderStatus.CREADO) {
            throw new IllegalStateException("No se puede despachar un pedido que no fue aceptado");
        }
        if (!current.canTransitionTo(next)) {
            throw new IllegalStateException("TransiciÃ³n no permitida: " + current + " -> " + next);
        }

        if (next == OrderStatus.ACEPTADO) {
            discountStock(o);
        } else if (next == OrderStatus.CANCELADO && current != OrderStatus.CREADO) {
            restoreStock(o);   // ya se habÃ­a descontado al aceptar
        }

        o.setStatus(next);
        eventPublisher.publicarCambioEstado(o);
        return OrderResponse.from(o);
    }

    private void discountStock(Order o) {
        for (OrderItem item : o.getItems()) {
            Product p = item.getProduct();
            if (p.getStock() < item.getQuantity()) {
                throw new IllegalStateException("Stock insuficiente para '" + p.getName()
                        + "' (disponible: " + p.getStock() + ", pedido: " + item.getQuantity() + ")");
            }
            p.setStock(p.getStock() - item.getQuantity());
        }
    }

    private void restoreStock(Order o) {
        for (OrderItem item : o.getItems()) {
            Product p = item.getProduct();
            p.setStock(p.getStock() + item.getQuantity());
        }
    }

    private Order find(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido " + id + " no encontrado"));
    }

    private boolean seesAll(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_Admin") || a.getAuthority().equals("ROLE_Operador"));
    }
}
