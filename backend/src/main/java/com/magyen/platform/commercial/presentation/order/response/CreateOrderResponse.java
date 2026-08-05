package com.magyen.platform.commercial.presentation.order.response;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Respuesta HTTP tras crear una Orden exitosamente.
 */
public record CreateOrderResponse(
        UUID orderId,
        String orderNumber,
        String status,
        LocalDate confirmationDate
) {
}
