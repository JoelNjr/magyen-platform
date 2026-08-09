package com.magyen.platform.commercial.presentation.quotation.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta HTTP con el detalle completo de una cotización.
 */
public record GetQuotationResponse(
        UUID quotationId,
        Long quotationNumber,
        UUID customerId,
        LocalDate creationDate,
        LocalDate deliveryDate,
        String status,
        String salesperson,
        String observations,
        List<QuotationItemResponse> items,
        BigDecimal totalAmount
) {
}
