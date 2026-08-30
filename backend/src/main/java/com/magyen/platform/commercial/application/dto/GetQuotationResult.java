package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.QuotationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Resultado del caso de uso que consulta una cotización completa.
 */
public record GetQuotationResult(
        UUID quotationId,
        Long quotationNumber,
        UUID customerId,
        LocalDate creationDate,
        LocalDate deliveryDate,
        QuotationStatus status,
        UUID sellerId,
        String sellerName,
        String observations,
        List<QuotationItemResult> items,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        UUID orderId
) {
}
