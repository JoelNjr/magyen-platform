package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Modelo de lectura para el PDF de remisión. No es una factura.
 */
public record RemissionPdfDocument(
        String orderNumber,
        String description,
        LocalDate confirmationDate,
        LocalDate promisedDeliveryDate,
        String customerName,
        String sellerName,
        String observations,
        List<CommercialDocumentProductLine> lines,
        BigDecimal totalAmount,
        BigDecimal collectedAmount,
        BigDecimal outstandingAmount
) {
}
