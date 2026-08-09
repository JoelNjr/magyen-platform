package com.magyen.platform.commercial.presentation.order.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta HTTP con las Órdenes existentes.
 */
public record GetOrdersResponse(
        List<OrderResponse> orders
) {

    /**
     * Orden expuesta por la API de consulta de listado.
     */
    public record OrderResponse(
            UUID orderId,
            String orderNumber,
            UUID customerId,
            UUID quotationId,
            LocalDate confirmationDate,
            String status,
            String salesperson,
            String observations,
            BigDecimal totalAmount
    ) {
    }
}
