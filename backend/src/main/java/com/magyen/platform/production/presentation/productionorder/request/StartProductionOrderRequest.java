package com.magyen.platform.production.presentation.productionorder.request;

import java.time.LocalDate;

/**
 * Payload HTTP opcional para iniciar una Orden de Producción con fecha real.
 */
public record StartProductionOrderRequest(
        LocalDate actualStartDate
) {
}
