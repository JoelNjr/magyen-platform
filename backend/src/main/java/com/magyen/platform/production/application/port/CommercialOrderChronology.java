package com.magyen.platform.production.application.port;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Fechas comerciales necesarias para validar la cronología de producción.
 */
public record CommercialOrderChronology(
        UUID orderId,
        LocalDate quotationDate,
        LocalDate confirmationDate,
        LocalDate deliveryDate
) {
}
