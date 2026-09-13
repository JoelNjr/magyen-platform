package com.magyen.platform.commercial.application.dto;

import java.util.UUID;

/**
 * Consulta de solo lectura del efecto de aplicar la cotización actual a su Orden.
 */
public record PreviewQuotationOrderSynchronizationQuery(UUID quotationId) {
}
