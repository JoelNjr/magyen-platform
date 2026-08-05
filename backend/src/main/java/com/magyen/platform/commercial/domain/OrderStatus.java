package com.magyen.platform.commercial.domain;

/**
 * Representa el ciclo de vida de una Orden comercial confirmada.
 */
public enum OrderStatus {

    CONFIRMED,
    IN_PRODUCTION,
    READY_FOR_DELIVERY,
    DELIVERED,
    CLOSED
}
