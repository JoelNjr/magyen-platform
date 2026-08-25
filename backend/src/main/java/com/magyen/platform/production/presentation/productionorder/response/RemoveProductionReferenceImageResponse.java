package com.magyen.platform.production.presentation.productionorder.response;

import java.util.UUID;

/**
 * Respuesta HTTP tras eliminar la imagen de referencia.
 */
public record RemoveProductionReferenceImageResponse(
        UUID productionOrderId,
        boolean hasReferenceImage
) {
}
