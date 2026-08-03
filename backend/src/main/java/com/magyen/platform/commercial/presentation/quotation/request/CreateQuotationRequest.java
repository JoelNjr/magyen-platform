package com.magyen.platform.commercial.presentation.quotation.request;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload HTTP para crear una cotización.
 */
public record CreateQuotationRequest(
        UUID customerId,
        LocalDate deliveryDate,
        String salesperson,
        String observations
) {
}
