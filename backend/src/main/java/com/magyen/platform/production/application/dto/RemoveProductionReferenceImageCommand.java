package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Entrada para eliminar la imagen de referencia de una Orden de Producción.
 */
public record RemoveProductionReferenceImageCommand(
        UUID productionOrderId
) {
}
