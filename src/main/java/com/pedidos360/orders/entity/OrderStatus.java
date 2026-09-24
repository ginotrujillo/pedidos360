package com.pedidos360.orders.entity;

public enum OrderStatus {
    CREADO, ACEPTADO, EN_PREPARACION, DESPACHADO, ENTREGADO, CANCELADO;

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case CREADO -> next == ACEPTADO || next == CANCELADO;
            case ACEPTADO -> next == EN_PREPARACION || next == CANCELADO;
            case EN_PREPARACION -> next == DESPACHADO || next == CANCELADO;
            case DESPACHADO -> next == ENTREGADO;
            case ENTREGADO, CANCELADO -> false;
        };
    }
}