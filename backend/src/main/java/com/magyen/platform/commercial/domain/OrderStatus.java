package com.magyen.platform.commercial.domain;

/**
 * Representa el ciclo de vida de una Orden comercial confirmada.
 */
public enum OrderStatus {

    CONFIRMED,
    IN_PRODUCTION,
    READY_FOR_DELIVERY,
    DELIVERED,
    CLOSED;

    /**
     * Contenido comercial editable hasta la entrega. DELIVERED y CLOSED quedan congelados.
     */
    public boolean allowsCommercialContentEditing() {
        return this == CONFIRMED || this == IN_PRODUCTION || this == READY_FOR_DELIVERY;
    }
}
