package com.magyen.platform.commercial.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para eliminar un producto de una cotización en borrador.
 */
public record RemoveQuotationItemCommand(
        UUID quotationId,
        UUID itemId
) {
}
