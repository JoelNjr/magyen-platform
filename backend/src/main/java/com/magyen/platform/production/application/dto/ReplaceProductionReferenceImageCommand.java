package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Entrada para cargar o reemplazar la imagen de referencia de una Orden de Producción.
 */
public record ReplaceProductionReferenceImageCommand(
        UUID productionOrderId,
        String originalFilename,
        String declaredContentType,
        byte[] content
) {
}
