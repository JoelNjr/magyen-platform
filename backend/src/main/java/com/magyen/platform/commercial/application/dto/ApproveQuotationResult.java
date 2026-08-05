package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.QuotationStatus;

import java.util.UUID;

/**
 * Resultado del caso de uso de aprobación de cotización.
 */
public record ApproveQuotationResult(
        UUID quotationId,
        QuotationStatus status
) {
}
