package com.magyen.platform.commercial.presentation.quotation.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Fecha de entrega de la cotización después de corregirla.
 */
public record UpdateQuotationDeliveryDateResponse(
        UUID quotationId,
        LocalDate creationDate,
        LocalDate deliveryDate,
        String status,
        BigDecimal totalAmount
) {
}
