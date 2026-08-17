package com.magyen.platform.commercial.presentation.order.request;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload HTTP para crear una Orden a partir de una Cotización aprobada.
 */
public record CreateOrderRequest(
        UUID quotationId,
        String orderNumber,
        String description,
        LocalDate confirmationDate,
        LocalDate deliveryDate,
        String observations
) {
}
