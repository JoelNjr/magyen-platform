package com.magyen.platform.commercial.presentation.quotation.request;

import java.time.LocalDate;

/**
 * Payload para corregir la fecha de entrega de una cotización.
 */
public record UpdateQuotationDeliveryDateRequest(
        LocalDate deliveryDate
) {
}
