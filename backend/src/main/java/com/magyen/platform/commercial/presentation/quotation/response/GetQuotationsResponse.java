package com.magyen.platform.commercial.presentation.quotation.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta HTTP con las cotizaciones existentes.
 */
public record GetQuotationsResponse(
        List<QuotationResponse> quotations
) {

    /**
     * Cotización expuesta por la API de consulta.
     */
    public record QuotationResponse(
            UUID quotationId,
            Long quotationNumber,
            UUID customerId,
            LocalDate creationDate,
            LocalDate deliveryDate,
            String status,
            UUID sellerId,
            String sellerName,
            String observations,
            BigDecimal totalAmount
    ) {
    }
}
