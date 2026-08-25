package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Resultado de cargar o reemplazar la imagen de referencia.
 */
public record ReplaceProductionReferenceImageResult(
        UUID productionOrderId,
        boolean hasReferenceImage
) {
}
