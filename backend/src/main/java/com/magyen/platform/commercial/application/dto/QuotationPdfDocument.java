package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Modelo de lectura para el PDF de cotización. Solo datos de negocio ya persistidos.
 */
public record QuotationPdfDocument(
        String quotationNumberDisplay,
        LocalDate quotationDate,
        LocalDate deliveryDate,
        String customerName,
        String sellerName,
        String observations,
        List<CommercialDocumentProductLine> lines,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {
}
