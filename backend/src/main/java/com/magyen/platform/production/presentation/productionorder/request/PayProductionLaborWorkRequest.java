package com.magyen.platform.production.presentation.productionorder.request;

import java.time.LocalDate;

/**
 * Payload HTTP opcional para pagar mano de obra de producción.
 */
public record PayProductionLaborWorkRequest(
        LocalDate paymentDate,
        String observation
) {
}
