package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Resultado de eliminar la imagen de referencia.
 */
public record RemoveProductionReferenceImageResult(
        UUID productionOrderId,
        boolean hasReferenceImage
) {
}
