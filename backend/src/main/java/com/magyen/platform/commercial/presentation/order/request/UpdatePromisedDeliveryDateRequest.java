package com.magyen.platform.commercial.presentation.order.request;

import java.time.LocalDate;

/**
 * Payload para corregir la fecha prometida. No es la fecha real de entrega.
 */
public record UpdatePromisedDeliveryDateRequest(
        LocalDate promisedDeliveryDate
) {
}
