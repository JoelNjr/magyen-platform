package com.magyen.platform.production.presentation.productionorder.request;

import java.time.LocalDate;

/**
 * Payload HTTP opcional para completar una Orden de Producción con fecha real.
 */
public record CompleteProductionOrderRequest(
        LocalDate actualCompletionDate
) {
}
