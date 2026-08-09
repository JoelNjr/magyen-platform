package com.magyen.platform.commercial.presentation.quotation.response;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Respuesta HTTP tras crear una cotización exitosamente.
 */
public record CreateQuotationResponse(
        UUID quotationId,
        Long quotationNumber,
        String status,
        LocalDate creationDate
) {
}
