package com.magyen.platform.commercial.application.dto;

import java.util.UUID;

/**
 * Entrada para duplicar un ítem dentro de su cotización.
 */
public record DuplicateQuotationItemCommand(
        UUID quotationId,
        UUID itemId
) {
}
