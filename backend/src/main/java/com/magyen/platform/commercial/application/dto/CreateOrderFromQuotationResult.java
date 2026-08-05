package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.OrderStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado del caso de uso de creación de Orden desde Cotización.
 */
public record CreateOrderFromQuotationResult(
        UUID orderId,
        String orderNumber,
        OrderStatus status,
        LocalDate confirmationDate
) {
}
