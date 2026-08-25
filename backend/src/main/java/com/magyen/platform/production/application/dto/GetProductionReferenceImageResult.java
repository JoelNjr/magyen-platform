package com.magyen.platform.production.application.dto;

/**
 * Imagen de referencia lista para entrega HTTP autenticada.
 */
public record GetProductionReferenceImageResult(
        byte[] content,
        String contentType
) {
}
