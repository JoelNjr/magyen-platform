package com.magyen.platform.commercial.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada para corregir la fecha prometida de una orden.
 */
public record UpdatePromisedDeliveryDateCommand(
        UUID orderId,
        LocalDate promisedDeliveryDate
) {
}
