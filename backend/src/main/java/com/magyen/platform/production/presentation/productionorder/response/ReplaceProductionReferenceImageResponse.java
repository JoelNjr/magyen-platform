package com.magyen.platform.production.presentation.productionorder.response;

import java.util.UUID;

/**
 * Respuesta HTTP tras cargar o reemplazar la imagen de referencia.
 */
public record ReplaceProductionReferenceImageResponse(
        UUID productionOrderId,
        boolean hasReferenceImage
) {
}
