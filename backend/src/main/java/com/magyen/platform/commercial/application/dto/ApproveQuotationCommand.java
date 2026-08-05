package com.magyen.platform.commercial.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para aprobar una cotización.
 */
public record ApproveQuotationCommand(
        UUID quotationId
) {
}
