package com.magyen.platform.commercial.presentation.quotation.response;

import java.util.UUID;

/**
 * Respuesta HTTP tras aprobar una cotización exitosamente.
 */
public record ApproveQuotationResponse(
        UUID quotationId,
        String status
) {
}
