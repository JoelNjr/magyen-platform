package com.magyen.platform.commercial.presentation.order.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Fecha prometida después de la corrección, junto con los datos que no deben cambiar.
 */
public record UpdatePromisedDeliveryDateResponse(
        UUID orderId,
        LocalDate confirmationDate,
        LocalDate promisedDeliveryDate,
        LocalDate actualDeliveryDate,
        String status,
        BigDecimal totalAmount
) {
}
