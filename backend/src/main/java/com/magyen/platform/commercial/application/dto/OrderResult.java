package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representación de una Orden para casos de uso de consulta de listado.
 */
public record OrderResult(
        UUID orderId,
        String orderNumber,
        UUID customerId,
        UUID quotationId,
        LocalDate confirmationDate,
        OrderStatus status,
        String salesperson,
        String observations,
        BigDecimal totalAmount
) {
}
