package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.QuotationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Fecha de entrega persistida de la cotización. No incluye datos de una orden asociada.
 */
public record UpdateQuotationDeliveryDateResult(
        UUID quotationId,
        LocalDate creationDate,
        LocalDate deliveryDate,
        QuotationStatus status,
        BigDecimal totalAmount
) {
}
