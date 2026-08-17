package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.QuotationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representación de una cotización para casos de uso de consulta.
 */
public record QuotationResult(
        UUID quotationId,
        Long quotationNumber,
        UUID customerId,
        LocalDate creationDate,
        LocalDate deliveryDate,
        QuotationStatus status,
        UUID sellerId,
        String sellerName,
        String observations,
        BigDecimal totalAmount
) {
}
