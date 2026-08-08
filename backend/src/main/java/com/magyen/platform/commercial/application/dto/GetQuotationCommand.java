package com.magyen.platform.commercial.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para consultar una cotización por identificador.
 */
public record GetQuotationCommand(
        UUID quotationId
) {
}
