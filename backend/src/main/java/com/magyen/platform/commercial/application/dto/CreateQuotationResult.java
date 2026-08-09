package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.QuotationStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado del caso de uso de creación de cotización.
 */
public record CreateQuotationResult(
        UUID quotationId,
        Long quotationNumber,
        QuotationStatus status,
        LocalDate creationDate
) {
}
