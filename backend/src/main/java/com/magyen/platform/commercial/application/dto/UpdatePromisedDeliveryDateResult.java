package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Fecha prometida persistida. La fecha real y el estado se devuelven para confirmar que no cambiaron.
 */
public record UpdatePromisedDeliveryDateResult(
        UUID orderId,
        LocalDate confirmationDate,
        LocalDate promisedDeliveryDate,
        LocalDate actualDeliveryDate,
        OrderStatus status,
        BigDecimal totalAmount
) {
}
